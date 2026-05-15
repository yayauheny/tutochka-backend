package yayauheny.by.importing.service

import java.util.UUID
import java.util.concurrent.CancellationException
import kotlinx.serialization.json.JsonObject
import org.jooq.DSLContext
import org.postgresql.util.PSQLException
import yayauheny.by.importing.exception.ImportException
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.model.ImportBatchSummary
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.model.SourceLocationHint
import yayauheny.by.importing.provider.ImportEnvelopeParsingException
import yayauheny.by.importing.repository.ImportInboxRepository
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.util.transactionSuspend

class ImportBatchProcessor(
    private val ctx: DSLContext,
    private val registry: ImportAdapterRegistry,
    private val importCityResolver: ImportCityResolver,
    private val importInboxRepository: ImportInboxRepository,
    private val importPipeline: ImportPipeline
) {
    suspend fun process(
        provider: ImportProvider,
        payloadType: ImportPayloadType,
        cityId: UUID?,
        items: List<JsonObject>,
        chunkSize: Int
    ): ImportBatchSummary {
        val adapter = registry.get(provider)
        val resolvedCities = resolveCityAssignments(adapter, cityId, items)
        val results = mutableListOf<ImportPipelineResult>()
        var firstImportId: UUID? = null

        items.chunked(chunkSize).forEachIndexed { chunkIndex, chunkItems ->
            val chunkOffset = chunkIndex * chunkSize
            ctx.transactionSuspend { txCtx ->
                chunkItems.forEachIndexed { localIndex, item ->
                    val index = chunkOffset + localIndex
                    val resolvedCity = resolvedCities[index]
                    if (resolvedCity.errorMessage != null || resolvedCity.cityId == null) {
                        val metadata = adapter.extractInboxMetadata(item)
                        val importId =
                            importInboxRepository.upsertPendingInTx(
                                txCtx = txCtx,
                                provider = provider,
                                payloadType = payloadType,
                                cityId = null,
                                metadata = metadata,
                                rawPayload = item
                            )
                        if (firstImportId == null) {
                            firstImportId = importId
                        }

                        importInboxRepository.markFailedInTx(txCtx, importId, resolvedCity.errorMessage ?: "Unable to resolve city")
                        results +=
                            ImportPipelineResult(
                                outcome = ImportStatus.FAILED,
                                providerExternalId = metadata.externalId,
                                errorCode = "CITY_RESOLUTION_FAILED",
                                errorMessage = resolvedCity.errorMessage ?: "Unable to resolve city"
                            )
                        return@forEachIndexed
                    }

                    val context = ImportAdapterContext(payloadType = payloadType, cityId = resolvedCity.cityId)
                    val envelope =
                        try {
                            adapter.parseEnvelope(item, context)
                        } catch (error: Throwable) {
                            when (error) {
                                is CancellationException -> throw error
                                is InvalidImportPayload,
                                is ImportEnvelopeParsingException -> {
                                    val metadata = (error as? ImportEnvelopeParsingException)?.inboxMetadata
                                    val importId =
                                        importInboxRepository.upsertPendingInTx(
                                            txCtx = txCtx,
                                            provider = provider,
                                            payloadType = payloadType,
                                            cityId = resolvedCity.cityId,
                                            metadata = metadata,
                                            rawPayload = item
                                        )
                                    if (firstImportId == null) {
                                        firstImportId = importId
                                    }

                                    val errorMessage = error.message ?: error.javaClass.simpleName
                                    importInboxRepository.markFailedInTx(txCtx, importId, errorMessage)
                                    results +=
                                        ImportPipelineResult(
                                            outcome = ImportStatus.FAILED,
                                            providerExternalId = metadata?.externalId,
                                            errorCode = "INVALID_PAYLOAD",
                                            errorMessage = errorMessage
                                        )
                                    return@forEachIndexed
                                }
                                is RuntimeException -> throw error
                                else -> throw error
                            }
                        }

                    val importId =
                        importInboxRepository.upsertPendingInTx(
                            txCtx = txCtx,
                            provider = provider,
                            payloadType = payloadType,
                            cityId = resolvedCity.cityId,
                            metadata = envelope.inboxMetadata,
                            rawPayload = item
                        )
                    if (firstImportId == null) {
                        firstImportId = importId
                    }
                    val result =
                        try {
                            inSavepoint(txCtx) { nestedTx ->
                                importPipeline.importEnvelopeInTx(nestedTx, envelope)
                            }
                        } catch (error: Throwable) {
                            when (error) {
                                is CancellationException -> throw error
                                is ImportException,
                                is PSQLException ->
                                    ImportPipelineResult(
                                        outcome = ImportStatus.FAILED,
                                        providerExternalId = envelope.inboxMetadata.externalId,
                                        errorCode = "IMPORT_FAILED",
                                        errorMessage = error.message ?: error.javaClass.simpleName
                                    )
                                is RuntimeException -> throw error
                                else -> throw error
                            }
                        }

                    if (result.outcome == ImportStatus.FAILED) {
                        importInboxRepository.markFailedInTx(txCtx, importId, result.errorMessage ?: "Unknown error")
                    } else {
                        importInboxRepository.markSuccessInTx(
                            txCtx = txCtx,
                            id = importId,
                            buildingId = result.buildingId,
                            restroomId = requireNotNull(result.restroomId)
                        )
                    }
                    results += result
                    if (results.size != index + 1) {
                        error("Unexpected batch result ordering")
                    }
                }
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

    private suspend fun resolveCityAssignments(
        adapter: yayauheny.by.importing.provider.ImportSourceAdapter,
        overrideCityId: UUID?,
        items: List<JsonObject>
    ): List<ResolvedCityAssignment> {
        if (overrideCityId != null) {
            return List(items.size) { ResolvedCityAssignment(cityId = overrideCityId) }
        }

        val metadataCache = mutableMapOf<MetadataKey, ImportCityResolver.MetadataCityResolution>()
        return items.map { item ->
            val sourceLocation = adapter.extractSourceLocation(item)
            val metadataResolution =
                sourceLocation.country
                    ?.takeIf { it.isNotBlank() }
                    ?.let { country ->
                        sourceLocation.city
                            ?.takeIf { it.isNotBlank() }
                            ?.let { city ->
                                metadataCache.getOrPut(MetadataKey(country = country, city = city)) {
                                    importCityResolver.resolveByMetadata(countryName = country, cityName = city)
                                }
                            }
                    }

            when (metadataResolution) {
                is ImportCityResolver.MetadataCityResolution.Resolved ->
                    ResolvedCityAssignment(cityId = metadataResolution.cityId)

                is ImportCityResolver.MetadataCityResolution.NeedsNearestFallback ->
                    resolveNearestCity(
                        sourceLocation = sourceLocation,
                        countryId = metadataResolution.countryId,
                        reason = metadataResolution.reason
                    )

                null ->
                    resolveNearestCity(
                        sourceLocation = sourceLocation,
                        countryId = null,
                        reason = "source city metadata is missing"
                    )
            }
        }
    }

    private suspend fun resolveNearestCity(
        sourceLocation: SourceLocationHint,
        countryId: UUID?,
        reason: String
    ): ResolvedCityAssignment {
        val cityId =
            importCityResolver.resolveNearest(
                lat = sourceLocation.lat,
                lng = sourceLocation.lng,
                countryId = countryId
            )

        return if (cityId != null) {
            ResolvedCityAssignment(cityId = cityId)
        } else {
            ResolvedCityAssignment(
                errorMessage = buildCityResolutionError(sourceLocation, reason)
            )
        }
    }

    private fun buildCityResolutionError(
        sourceLocation: SourceLocationHint,
        reason: String
    ): String {
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

    private fun <T> inSavepoint(
        txCtx: DSLContext,
        block: (DSLContext) -> T
    ): T =
        txCtx.connectionResult { connection ->
            val savepoint = connection.setSavepoint()
            try {
                val result = block(txCtx)
                connection.releaseSavepoint(savepoint)
                result
            } catch (error: Throwable) {
                connection.rollback(savepoint)
                throw error
            }
        }

    private data class MetadataKey(
        val country: String,
        val city: String
    )

    private data class ResolvedCityAssignment(
        val cityId: UUID? = null,
        val errorMessage: String? = null
    )
}
