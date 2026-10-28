package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import yayauheny.by.entity.BuildingEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.importing.dedup.ProviderKeyResolver
import yayauheny.by.importing.mapper.RestroomImportMapper
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.NormalizedRestroomCandidate
import yayauheny.by.model.imports.ImportStatus
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.util.point
import java.util.UUID

@Service
class ImportPipeline(
    private val buildingRepository: BuildingJpaRepository,
    private val cityRepository: CityJpaRepository,
    private val restroomRepository: RestroomJpaRepository,
    private val entityManager: EntityManager,
    private val jdbcTemplate: JdbcTemplate,
    private val objectMapper: ObjectMapper
) {
    fun importEnvelope(envelope: ProviderImportEnvelope): ImportPipelineResult {
        val candidate = envelope.command
        val providerKey = ProviderKeyResolver.providerKey(candidate.provider)
        val building = resolveBuilding(candidate, providerKey)
        val restroomMatchKey = MatchKeyGenerator.restroomMatchKey(
            cityId = candidate.cityId,
            buildingId = building?.id,
            address = candidate.address,
            name = candidate.name,
            lat = candidate.lat,
            lon = candidate.lng,
            locationType = candidate.locationType
        )

        restroomRepository.findByOriginProviderAndOriginId(candidate.provider, candidate.providerObjectId)?.let { existing ->
            val updated = updateRestroom(existing, candidate, building, providerKey, restroomMatchKey)
            return ImportPipelineResult(
                outcome = ImportStatus.UPDATED,
                providerExternalId = candidate.providerObjectId,
                restroomId = updated.id,
                buildingId = updated.building?.id
            )
        }

        if (providerKey != null) {
            restroomRepository.findByExternalMap(providerKey, candidate.providerObjectId)?.let { existing ->
                return ImportPipelineResult(
                    outcome = ImportStatus.SKIPPED_DUPLICATE,
                    providerExternalId = candidate.providerObjectId,
                    restroomId = existing.id,
                    buildingId = existing.building?.id,
                    duplicateOfRestroomId = existing.id,
                    duplicateReason = "EXACT_EXTERNAL_MAP"
                )
            }
        }

        restroomMatchKey?.let { key ->
            restroomRepository.findByRestroomMatchKey(key)
        }?.let { existing ->
            if (providerKey != null) {
                existing.applyProviderId(providerKey, candidate.providerObjectId)
                restroomRepository.saveAndFlush(existing)
            }
            return ImportPipelineResult(
                outcome = ImportStatus.LINKED_DUPLICATE,
                providerExternalId = candidate.providerObjectId,
                restroomId = existing.id,
                buildingId = existing.building?.id,
                duplicateOfRestroomId = existing.id,
                duplicateReason = "EXACT_RESTROOM_MATCH_KEY"
            )
        }

        val created = createRestroom(candidate, building, providerKey, restroomMatchKey)
        if (providerKey != null) {
            logNearbyDuplicateSuspicions(candidate.provider, candidate.providerObjectId, candidate.cityId, created.id!!, candidate.lat, candidate.lng)
        }

        return ImportPipelineResult(
            outcome = ImportStatus.CREATED,
            providerExternalId = candidate.providerObjectId,
            restroomId = created.id,
            buildingId = created.building?.id
        )
    }

    private fun resolveBuilding(candidate: NormalizedRestroomCandidate, providerKey: String?): BuildingEntity? {
        val context = candidate.buildingContext ?: return null
        val matchKey = MatchKeyGenerator.buildingMatchKey(candidate.cityId, context.address, candidate.lat, candidate.lng)

        if (providerKey != null) {
            buildingRepository.findByExternalId(providerKey, context.externalId)?.let { return it }
        }

        matchKey?.let {
            buildingRepository.findByBuildingMatchKey(it)
        }?.let { existing ->
            if (providerKey != null) {
                existing.applyProviderId(providerKey, context.externalId)
                return buildingRepository.saveAndFlush(existing)
            }
            return existing
        }

        val cityReference = entityManager.getReference(yayauheny.by.entity.CityEntity::class.java, candidate.cityId)
        val entity = BuildingEntity().apply {
            city = cityReference
            name = context.name
            address = context.address
            buildingType = candidate.placeType.name.lowercase()
            workTime = context.workTime.toMapOrNull()
            coordinates = point(candidate.lng, candidate.lat)
            applyProviderId(providerKey, context.externalId)
            buildingMatchKey = matchKey
        }
        return buildingRepository.saveAndFlush(entity)
    }

    private fun createRestroom(
        candidate: NormalizedRestroomCandidate,
        building: BuildingEntity?,
        providerKey: String?,
        restroomMatchKey: String?
    ): RestroomEntity {
        val dto = RestroomImportMapper.toCreateDto(candidate, buildingId = building?.id, inheritBuildingSchedule = building != null)
        val entity = RestroomEntity().apply {
            applyImportedFields(dto, candidate, building, providerKey)
            phones = dto.phones?.values?.map { it.toString() }
            subwayStation = null
            isHidden = false
            accessNote = dto.accessNote
            directionGuide = dto.directionGuide
            this.restroomMatchKey = restroomMatchKey
        }
        return restroomRepository.saveAndFlush(entity)
    }

    private fun updateRestroom(
        entity: RestroomEntity,
        candidate: NormalizedRestroomCandidate,
        building: BuildingEntity?,
        providerKey: String?,
        restroomMatchKey: String?
    ): RestroomEntity {
        val dto = RestroomImportMapper.toCreateDto(candidate, buildingId = building?.id, inheritBuildingSchedule = building != null)
        entity.applyImportedFields(dto, candidate, building, providerKey)
        entity.restroomMatchKey = restroomMatchKey
        return restroomRepository.saveAndFlush(entity)
    }

    private fun RestroomEntity.applyImportedFields(
        dto: RestroomCreateDto,
        candidate: NormalizedRestroomCandidate,
        building: BuildingEntity?,
        providerKey: String?
    ) {
        city = entityManager.getReference(yayauheny.by.entity.CityEntity::class.java, candidate.cityId)
        this.building = building
        name = dto.name
        placeType = dto.placeType
        address = dto.address
        workTime = dto.workTime
        feeType = dto.feeType ?: feeType
        genderType = dto.genderType ?: genderType
        accessibilityType = dto.accessibilityType
        status = dto.status
        amenities = dto.amenities
        inheritBuildingSchedule = dto.inheritBuildingSchedule
        coordinates = point(dto.coordinates.lon, dto.coordinates.lat)
        dataSource = DataSourceType.IMPORT
        locationType = dto.locationType
        originProvider = dto.originProvider
        originId = dto.originId
        applyProviderId(providerKey, candidate.providerObjectId)
    }

    private fun BuildingEntity.applyProviderId(providerKey: String?, externalId: String) {
        when (providerKey) {
            "2gis" -> if (twoGisId.isNullOrBlank()) twoGisId = externalId
            "yandex" -> if (yandexId.isNullOrBlank()) yandexId = externalId
            "google" -> if (googleId.isNullOrBlank()) googleId = externalId
            "apple" -> if (appleId.isNullOrBlank()) appleId = externalId
        }
    }

    private fun RestroomEntity.applyProviderId(providerKey: String?, externalId: String) {
        when (providerKey) {
            "2gis" -> if (twoGisId.isNullOrBlank()) twoGisId = externalId
            "yandex" -> if (yandexId.isNullOrBlank()) yandexId = externalId
            "google" -> if (googleId.isNullOrBlank()) googleId = externalId
            "apple" -> if (appleId.isNullOrBlank()) appleId = externalId
        }
    }

    private fun logNearbyDuplicateSuspicions(
        provider: ImportProvider,
        externalId: String,
        cityId: UUID,
        candidateRestroomId: UUID,
        lat: Double,
        lng: Double
    ) {
        jdbcTemplate.query(
            """
            SELECT id, ST_Distance(
                coordinates::geography,
                ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
            ) AS distance_m
            FROM restrooms
            WHERE city_id = ?
              AND id <> ?
              AND ST_DWithin(
                    coordinates::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    15.0
              )
            ORDER BY distance_m ASC
            LIMIT 3
            """.trimIndent(),
            { rs ->
                jdbcTemplate.update(
                    """
                    INSERT INTO restroom_duplicate_suspicions (
                        existing_restroom_id,
                        candidate_restroom_id,
                        distance_m,
                        reason,
                        status,
                        provider,
                        external_id
                    ) VALUES (?, ?, ?, ?, ?, ?, ?)
                    """.trimIndent(),
                    UUID.fromString(rs.getString("id")),
                    candidateRestroomId,
                    rs.getDouble("distance_m"),
                    "NEARBY_IMPORTED_RESTROOM",
                    "PENDING",
                    provider.name,
                    externalId
                )
            },
            lng,
            lat,
            cityId,
            candidateRestroomId,
            lng,
            lat
        )
    }

    private fun JsonNode?.toMapOrNull(): Map<String, Any>? =
        this?.takeIf { !it.isNull }?.let { objectMapper.convertValue(it, Map::class.java) as Map<String, Any> }

}
