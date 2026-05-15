package yayauheny.by.importing.service

import java.util.UUID
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jooq.DSLContext
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.importing.dedup.ProviderKeyResolver
import yayauheny.by.importing.mapper.RestroomImportMapper
import yayauheny.by.importing.model.ImportOriginKey
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.importing.repository.BuildingImportRepository
import yayauheny.by.importing.repository.DuplicateSuspicionRepository
import yayauheny.by.importing.repository.RestroomImportRepository
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.ImportStatus
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.repository.impl.SubwayRepositoryImpl
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.distanceGeographyTo
import yayauheny.by.util.withinGeographyDistanceOf

class ImportPipeline(
    private val buildingImportRepository: BuildingImportRepository,
    private val restroomImportRepository: RestroomImportRepository,
    private val duplicateSuspicionRepository: DuplicateSuspicionRepository
) {
    companion object {
        private const val NEARBY_DUPLICATE_SUSPICION_DISTANCE_METERS = 15.0
        private const val MAX_NEARBY_DUPLICATE_SUSPICIONS = 3
    }

    fun importEnvelopeInTx(
        txCtx: DSLContext,
        envelope: ProviderImportEnvelope
    ): ImportPipelineResult {
        val candidate = envelope.command
        val providerKey = ProviderKeyResolver.providerKey(candidate.provider)

        val buildingMatchKey =
            candidate.buildingContext?.let {
                MatchKeyGenerator.buildingMatchKey(
                    cityId = candidate.cityId,
                    address = it.address,
                    lat = candidate.lat,
                    lon = candidate.lng
                )
            }
        val building =
            resolveBuilding(
                txCtx = txCtx,
                providerKey = providerKey,
                candidate = candidate,
                buildingMatchKey = buildingMatchKey
            )

        val restroomMatchKey =
            MatchKeyGenerator.restroomMatchKey(
                cityId = candidate.cityId,
                buildingId = building?.id,
                address = candidate.address,
                name = candidate.name,
                lat = candidate.lat,
                lon = candidate.lng,
                locationType = candidate.locationType
            )

        return importRestroom(
            txCtx = txCtx,
            providerKey = providerKey,
            candidate = candidate,
            resolvedBuildingId = building?.id,
            restroomMatchKey = restroomMatchKey
        )
    }

    private fun resolveBuilding(
        txCtx: DSLContext,
        providerKey: String?,
        candidate: NormalizedRestroomCandidate,
        buildingMatchKey: String?
    ): BuildingResponseDto? {
        val context = candidate.buildingContext ?: return null
        val resolvedProviderKey = providerKey ?: return null

        buildingImportRepository
            .findByExternalIdsInTx(txCtx, resolvedProviderKey, listOf(context.externalId))
            .firstOrNull()
            ?.let { return it }

        buildingMatchKey
            ?.let { key ->
                buildingImportRepository.findByMatchKeysInTx(txCtx, listOf(key)).firstOrNull()
            }?.let { existing ->
                return buildingImportRepository.linkExternalIdInTx(txCtx, existing.id, resolvedProviderKey, context.externalId)
            }

        val createDto =
            BuildingCreateDto(
                cityId = candidate.cityId,
                name = context.name,
                address = context.address,
                buildingType = candidate.placeType,
                workTime = context.workTime,
                coordinates = Coordinates(lat = candidate.lat, lon = candidate.lng),
                externalIds = buildJsonObject { put(resolvedProviderKey, JsonPrimitive(context.externalId)) }
            )

        return try {
            buildingImportRepository
                .upsertImportedBuildingInTx(
                    txCtx = txCtx,
                    provider = resolvedProviderKey,
                    externalId = context.externalId,
                    createDto = createDto,
                    matchKey = buildingMatchKey
                ).building
        } catch (error: PSQLException) {
            if (error.sqlState != PSQLState.UNIQUE_VIOLATION.state || buildingMatchKey == null) {
                throw error
            }

            val concurrentMatch =
                buildingImportRepository
                    .findByMatchKeysInTx(txCtx, listOf(buildingMatchKey))
                    .firstOrNull() ?: throw error

            buildingImportRepository.linkExternalIdInTx(txCtx, concurrentMatch.id, resolvedProviderKey, context.externalId)
        }
    }

    private fun importRestroom(
        txCtx: DSLContext,
        providerKey: String?,
        candidate: NormalizedRestroomCandidate,
        resolvedBuildingId: UUID?,
        restroomMatchKey: String?
    ): ImportPipelineResult {
        val createDto =
            RestroomImportMapper.toCreateDto(
                candidate = candidate,
                buildingId = resolvedBuildingId,
                inheritBuildingSchedule = resolvedBuildingId != null
            )

        val originId = requireNotNull(createDto.originId) { "originId is required for provider=${candidate.provider}" }
        val originKey = ImportOriginKey(createDto.originProvider, originId)

        restroomImportRepository.findByOriginsInTx(txCtx, listOf(originKey)).firstOrNull()?.let {
            val upserted = restroomImportRepository.upsertImportedRestroomInTx(txCtx, createDto, restroomMatchKey)
            setNearestStation(txCtx, upserted.restroom.id, candidate.lat, candidate.lng)
            return ImportPipelineResult(
                outcome = if (upserted.created) ImportStatus.CREATED else ImportStatus.UPDATED,
                providerExternalId = candidate.providerObjectId,
                restroomId = upserted.restroom.id,
                buildingId = upserted.restroom.buildingId
            )
        }

        if (providerKey != null) {
            restroomImportRepository
                .findByExternalMapsInTx(txCtx, providerKey, listOf(candidate.providerObjectId))
                .firstOrNull()
                ?.let { existing ->
                    return ImportPipelineResult(
                        outcome = ImportStatus.SKIPPED_DUPLICATE,
                        providerExternalId = candidate.providerObjectId,
                        restroomId = existing.id,
                        buildingId = existing.buildingId,
                        duplicateOfRestroomId = existing.id,
                        duplicateReason = "EXACT_EXTERNAL_MAP"
                    )
                }
        }

        restroomMatchKey
            ?.let { key ->
                restroomImportRepository.findByMatchKeysInTx(txCtx, listOf(key)).firstOrNull()
            }?.let { existing ->
                val linked =
                    if (providerKey == null) {
                        existing
                    } else {
                        val currentExternalId =
                            existing.externalMaps
                                ?.get(providerKey)
                                ?.toString()
                                ?.trim('"')
                        if (currentExternalId == candidate.providerObjectId) {
                            existing
                        } else {
                            restroomImportRepository.linkExternalMapInTx(txCtx, existing.id, providerKey, candidate.providerObjectId)
                        }
                    }

                return ImportPipelineResult(
                    outcome = ImportStatus.LINKED_DUPLICATE,
                    providerExternalId = candidate.providerObjectId,
                    restroomId = linked.id,
                    buildingId = linked.buildingId,
                    duplicateOfRestroomId = existing.id,
                    duplicateReason = "EXACT_RESTROOM_MATCH_KEY"
                )
            }

        val upserted =
            try {
                inSavepoint(txCtx) { nestedTx ->
                    restroomImportRepository.upsertImportedRestroomInTx(nestedTx, createDto, restroomMatchKey)
                }
            } catch (error: PSQLException) {
                if (!isRestroomMatchKeyUniqueViolation(error, restroomMatchKey)) {
                    throw error
                }

                val canonical =
                    restroomMatchKey
                        ?.let { key ->
                            restroomImportRepository.findByMatchKeysInTx(txCtx, listOf(key)).firstOrNull()
                        } ?: throw error

                val linked =
                    if (providerKey == null) {
                        canonical
                    } else {
                        val currentExternalId =
                            canonical.externalMaps
                                ?.get(providerKey)
                                ?.toString()
                                ?.trim('"')
                        if (currentExternalId == candidate.providerObjectId) {
                            canonical
                        } else {
                            restroomImportRepository.linkExternalMapInTx(txCtx, canonical.id, providerKey, candidate.providerObjectId)
                        }
                    }

                return ImportPipelineResult(
                    outcome = ImportStatus.LINKED_DUPLICATE,
                    providerExternalId = candidate.providerObjectId,
                    restroomId = linked.id,
                    buildingId = linked.buildingId,
                    duplicateOfRestroomId = canonical.id,
                    duplicateReason = "EXACT_RESTROOM_MATCH_KEY"
                )
            }
        setNearestStation(txCtx, upserted.restroom.id, candidate.lat, candidate.lng)

        if (upserted.created && providerKey != null) {
            logNearbyDuplicateSuspicions(
                txCtx = txCtx,
                provider = candidate.provider,
                providerExternalId = candidate.providerObjectId,
                candidate = candidate,
                candidateRestroomId = upserted.restroom.id
            )
        }

        return ImportPipelineResult(
            outcome = if (upserted.created) ImportStatus.CREATED else ImportStatus.UPDATED,
            providerExternalId = candidate.providerObjectId,
            restroomId = upserted.restroom.id,
            buildingId = upserted.restroom.buildingId
        )
    }

    private fun logNearbyDuplicateSuspicions(
        txCtx: DSLContext,
        provider: ImportProvider,
        providerExternalId: String,
        candidate: NormalizedRestroomCandidate,
        candidateRestroomId: UUID
    ) {
        val distanceField = RESTROOMS.COORDINATES.distanceGeographyTo(candidate.lat, candidate.lng).`as`("distance_m")
        txCtx
            .select(RESTROOMS.ID, distanceField)
            .from(RESTROOMS)
            .where(
                RESTROOMS.IS_DELETED.isFalse
                    .and(RESTROOMS.ID.ne(candidateRestroomId))
                    .and(RESTROOMS.CITY_ID.eq(candidate.cityId))
                    .and(
                        RESTROOMS.COORDINATES.withinGeographyDistanceOf(
                            candidate.lat,
                            candidate.lng,
                            NEARBY_DUPLICATE_SUSPICION_DISTANCE_METERS
                        )
                    )
            ).orderBy(distanceField.asc())
            .limit(MAX_NEARBY_DUPLICATE_SUSPICIONS)
            .fetch()
            .forEach { record ->
                duplicateSuspicionRepository.logNearbySuspicionInTx(
                    txCtx = txCtx,
                    existingRestroomId = requireNotNull(record.get(RESTROOMS.ID)),
                    candidateRestroomId = candidateRestroomId,
                    distanceMeters = requireNotNull(record.get(distanceField)),
                    provider = provider.name,
                    externalId = providerExternalId
                )
            }
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

    private fun setNearestStation(
        txCtx: DSLContext,
        restroomId: UUID,
        lat: Double,
        lon: Double
    ) {
        SubwayRepositoryImpl(txCtx).setNearestStationForRestroomInTx(
            txCtx = txCtx,
            restroomId = restroomId,
            lat = lat,
            lon = lon
        )
    }

    private fun isRestroomMatchKeyUniqueViolation(
        error: PSQLException,
        restroomMatchKey: String?
    ): Boolean =
        restroomMatchKey != null &&
            error.sqlState == PSQLState.UNIQUE_VIOLATION.state &&
            error.serverErrorMessage?.constraint == "restrooms_restroom_match_key_unique_idx"
}
