package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import yayauheny.by.importing.exception.ImportException
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.ImportBatchSummary
import yayauheny.by.importing.model.ImportEntityType
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.importing.provider.ImportEnvelopeParsingException
import yayauheny.by.model.enums.ImportJobStatus
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.ImportStatus
import java.sql.Timestamp
import java.time.Instant
import java.util.UUID

@Service
class ImportBatchProcessor(
    private val registry: ImportAdapterRegistry,
    private val importCityResolver: ImportCityResolver,
    private val importPipeline: ImportPipeline,
    private val restroomImportRepository: yayauheny.by.importing.repository.RestroomImportJpaRepository,
    private val transactionManager: PlatformTransactionManager,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    fun process(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        items: List<JsonNode>,
        chunkSize: Int
    ): ImportBatchSummary {
        val adapter = registry.get(provider)
        val itemTemplate = TransactionTemplate(transactionManager).apply {
            propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
        }
        val cityCache = mutableMapOf<MetadataKey, ImportCityResolver.MetadataCityResolution>()
        val results = mutableListOf<ImportPipelineResult>()
        var firstImportId: UUID? = null

        items.chunked(chunkSize).forEach { chunk ->
            chunk.forEach { item ->
                val outcome = itemTemplate.execute {
                    processItem(
                        adapter = adapter,
                        provider = provider,
                        payloadType = payloadType,
                        overrideCityId = cityId,
                        item = item,
                        cityCache = cityCache
                    )
                } ?: error("Import item transaction returned null")

                if (firstImportId == null) {
                    firstImportId = outcome.importId
                }
                results += outcome.result
            }
        }

        return ImportBatchSummary(
            importId = firstImportId ?: UUID.randomUUID(),
            totalProcessed = results.size,
            successful = results.count { it.outcome != ImportStatus.FAILED },
            failed = results.count { it.outcome == ImportStatus.FAILED },
            created = results.count { it.outcome == ImportStatus.CREATED },
            updated = results.count { it.outcome == ImportStatus.UPDATED },
            linkedDuplicates = results.count { it.outcome == ImportStatus.LINKED_DUPLICATE },
            skippedDuplicates = results.count { it.outcome == ImportStatus.SKIPPED_DUPLICATE },
            warnings = results.count { it.outcome == ImportStatus.LINKED_DUPLICATE || it.outcome == ImportStatus.SKIPPED_DUPLICATE },
            results = results
        )
    }

    private fun processItem(
        adapter: yayauheny.by.importing.provider.ImportSourceAdapter,
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        overrideCityId: UUID?,
        item: JsonNode,
        cityCache: MutableMap<MetadataKey, ImportCityResolver.MetadataCityResolution>
    ): ItemProcessingOutcome {
        val metadata = runCatching { adapter.extractInboxMetadata(item) }.getOrNull()
        val existingImport = metadata?.externalId?.let {
            restroomImportRepository.findByProviderAndEntityTypeAndExternalId(provider, ImportEntityType.PLACE.name.lowercase(), it)
        }
        val importId = upsertPending(
            provider = provider,
            payloadType = payloadType,
            cityId = overrideCityId,
            metadata = metadata,
            rawPayload = item
        )

        val resolvedCity = resolveCity(
            adapter = adapter,
            overrideCityId = overrideCityId,
            item = item,
            cityCache = cityCache
        )
        if (resolvedCity.cityId == null) {
            val errorMessage = resolvedCity.errorMessage ?: "Unable to resolve city"
            markFailed(importId, errorMessage)
            return ItemProcessingOutcome(
                importId = importId,
                result = ImportPipelineResult(
                    outcome = ImportStatus.FAILED,
                    providerExternalId = metadata?.externalId,
                    errorCode = "CITY_RESOLUTION_FAILED",
                    errorMessage = errorMessage
                )
            )
        }

        val samePayloadImport = existingImport
        if (samePayloadImport != null &&
            samePayloadImport.payloadHash == metadata?.payloadHash &&
            samePayloadImport.restroom?.id != null &&
            samePayloadImport.status == ImportJobStatus.SUCCESS
        ) {
            val existingRestroomId = requireNotNull(samePayloadImport.restroom?.id)
            markSuccess(importId, samePayloadImport.building?.id, existingRestroomId)
            return ItemProcessingOutcome(
                importId = importId,
                result = ImportPipelineResult(
                    outcome = ImportStatus.SKIPPED_DUPLICATE,
                    providerExternalId = metadata?.externalId,
                    restroomId = existingRestroomId,
                    buildingId = samePayloadImport.building?.id,
                    duplicateOfRestroomId = existingRestroomId,
                    duplicateReason = "SAME_PAYLOAD_HASH"
                )
            )
        }

        val envelope = try {
            adapter.parseEnvelope(item, ImportAdapterContext(payloadType = payloadType, cityId = resolvedCity.cityId))
        } catch (error: ImportEnvelopeParsingException) {
            val message = error.message ?: "Invalid payload"
            markFailed(importId, message)
            return ItemProcessingOutcome(
                importId = importId,
                result = ImportPipelineResult(
                    outcome = ImportStatus.FAILED,
                    providerExternalId = error.inboxMetadata?.externalId ?: metadata?.externalId,
                    errorCode = "INVALID_PAYLOAD",
                    errorMessage = message
                )
            )
        } catch (error: ImportException) {
            val message = error.message ?: "Invalid payload"
            markFailed(importId, message)
            return ItemProcessingOutcome(
                importId = importId,
                result = ImportPipelineResult(
                    outcome = ImportStatus.FAILED,
                    providerExternalId = metadata?.externalId,
                    errorCode = "INVALID_PAYLOAD",
                    errorMessage = message
                )
            )
        }

        val result = try {
            importPipeline.importEnvelope(envelope)
        } catch (error: ImportException) {
            ImportPipelineResult(
                outcome = ImportStatus.FAILED,
                providerExternalId = envelope.inboxMetadata.externalId,
                errorCode = "IMPORT_FAILED",
                errorMessage = error.message ?: "Import failed"
            )
        }

        if (result.outcome == ImportStatus.FAILED) {
            markFailed(importId, result.errorMessage ?: "Import failed")
        } else {
            markSuccess(importId, result.buildingId, requireNotNull(result.restroomId))
        }

        return ItemProcessingOutcome(importId = importId, result = result)
    }

    private fun resolveCity(
        adapter: yayauheny.by.importing.provider.ImportSourceAdapter,
        overrideCityId: UUID?,
        item: JsonNode,
        cityCache: MutableMap<MetadataKey, ImportCityResolver.MetadataCityResolution>
    ): ResolvedCityAssignment {
        if (overrideCityId != null) {
            return ResolvedCityAssignment(cityId = overrideCityId)
        }

        val sourceLocation = adapter.extractSourceLocation(item)
        val metadataResolution =
            sourceLocation.country
                ?.takeIf { it.isNotBlank() }
                ?.let { country ->
                    sourceLocation.city
                        ?.takeIf { it.isNotBlank() }
                        ?.let { city ->
                            cityCache.getOrPut(MetadataKey(country = country, city = city)) {
                                importCityResolver.resolveByMetadata(countryName = country, cityName = city)
                            }
                        }
                }

        return when (metadataResolution) {
            is ImportCityResolver.MetadataCityResolution.Resolved -> ResolvedCityAssignment(cityId = metadataResolution.cityId)
            is ImportCityResolver.MetadataCityResolution.NeedsNearestFallback ->
                resolveNearestCity(sourceLocation, metadataResolution.countryId, metadataResolution.reason)
            null -> resolveNearestCity(sourceLocation, null, "source city metadata is missing")
        }
    }

    private fun resolveNearestCity(
        sourceLocation: SourceLocationHint,
        countryId: UUID?,
        reason: String
    ): ResolvedCityAssignment {
        val cityId = importCityResolver.resolveNearest(sourceLocation.lat, sourceLocation.lng, countryId)
        return if (cityId != null) {
            ResolvedCityAssignment(cityId = cityId)
        } else {
            ResolvedCityAssignment(errorMessage = buildCityResolutionError(sourceLocation, reason))
        }
    }

    private fun buildCityResolutionError(sourceLocation: SourceLocationHint, reason: String): String {
        val details =
            listOfNotNull(
                sourceLocation.country?.let { "country=$it" },
                sourceLocation.city?.let { "city=$it" },
                sourceLocation.lat?.let { "lat=$it" },
                sourceLocation.lng?.let { "lng=$it" }
            ).joinToString(", ")
        return listOf("Unable to resolve city", "reason=$reason", details.takeIf { it.isNotBlank() })
            .filterNotNull()
            .joinToString(", ")
    }

    private fun upsertPending(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        metadata: yayauheny.by.importing.model.InboxMetadata?,
        rawPayload: JsonNode
    ): UUID {
        val now = Timestamp.from(Instant.now())
        val entityType = (metadata?.entityType ?: ImportEntityType.PLACE).name.lowercase()
        val payloadJson = objectMapper.writeValueAsString(rawPayload)
        val externalId = metadata?.externalId
        if (externalId == null) {
            val id = UUID.randomUUID()
            jdbcTemplate.update(
                """
                INSERT INTO restroom_imports (
                    id, provider, payload_type, city_id, entity_type, external_id, source_url,
                    scraped_at, payload_hash, raw_payload, building_id, restroom_id, status,
                    error_message, processed_at, attempts, last_attempt_at, next_retry_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, ?, ?, ?, ?)
                """.trimIndent(),
                id,
                provider.name,
                payloadType.name,
                cityId,
                entityType,
                null,
                metadata?.sourceUrl,
                metadata?.scrapedAt?.let(Timestamp::from),
                metadata?.payloadHash,
                payloadJson,
                null,
                null,
                ImportJobStatus.PENDING.name,
                null,
                null,
                1,
                now,
                null
            )
            return id
        }

        return jdbcTemplate.queryForObject(
            """
            INSERT INTO restroom_imports (
                id, provider, payload_type, city_id, entity_type, external_id, source_url,
                scraped_at, payload_hash, raw_payload, building_id, restroom_id, status,
                error_message, processed_at, attempts, last_attempt_at, next_retry_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT (provider, entity_type, external_id)
            WHERE external_id IS NOT NULL
            DO UPDATE SET
                payload_type = EXCLUDED.payload_type,
                city_id = EXCLUDED.city_id,
                source_url = EXCLUDED.source_url,
                scraped_at = EXCLUDED.scraped_at,
                payload_hash = EXCLUDED.payload_hash,
                raw_payload = EXCLUDED.raw_payload,
                building_id = NULL,
                restroom_id = NULL,
                status = EXCLUDED.status,
                error_message = NULL,
                processed_at = NULL,
                attempts = restroom_imports.attempts + 1,
                last_attempt_at = EXCLUDED.last_attempt_at,
                next_retry_at = NULL
            RETURNING id
            """.trimIndent(),
            UUID::class.java,
            UUID.randomUUID(),
            provider.name,
            payloadType.name,
            cityId,
            entityType,
            externalId,
            metadata.sourceUrl,
            metadata.scrapedAt?.let(Timestamp::from),
            metadata.payloadHash,
            payloadJson,
            null,
            null,
            ImportJobStatus.PENDING.name,
            null,
            null,
            1,
            now,
            null
        ) ?: error("Failed to upsert restroom_imports row")
    }

    private fun markSuccess(importId: UUID, buildingId: UUID?, restroomId: UUID) {
        jdbcTemplate.update(
            """
            UPDATE restroom_imports
            SET status = ?, building_id = ?, restroom_id = ?, processed_at = ?, error_message = NULL
            WHERE id = ?
            """.trimIndent(),
            ImportJobStatus.SUCCESS.name,
            buildingId,
            restroomId,
            Timestamp.from(Instant.now()),
            importId
        )
    }

    private fun markFailed(importId: UUID, errorMessage: String) {
        jdbcTemplate.update(
            """
            UPDATE restroom_imports
            SET status = ?, error_message = ?, processed_at = ?
            WHERE id = ?
            """.trimIndent(),
            ImportJobStatus.FAILED.name,
            errorMessage,
            Timestamp.from(Instant.now()),
            importId
        )
    }

    private data class MetadataKey(
        val country: String,
        val city: String
    )

    private data class ResolvedCityAssignment(
        val cityId: UUID? = null,
        val errorMessage: String? = null
    )

    private data class ItemProcessingOutcome(
        val importId: UUID,
        val result: ImportPipelineResult
    )
}
