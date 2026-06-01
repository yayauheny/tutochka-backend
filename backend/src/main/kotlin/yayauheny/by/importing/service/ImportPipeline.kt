package yayauheny.by.importing.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import yayauheny.by.entity.BuildingEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.importing.dedup.ProviderKeyResolver
import yayauheny.by.importing.mapper.RestroomImportMapper
import yayauheny.by.importing.model.ImportPipelineResult
import yayauheny.by.importing.provider.ProviderImportEnvelope
import yayauheny.by.model.enums.BuildingImportStatus
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.NormalizedRestroomCandidate
import yayauheny.by.model.imports.ImportStatus
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
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
                mergeExternalMaps(existing, providerKey, candidate.providerObjectId)
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
                mergeExternalIds(existing, providerKey, context.externalId)
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
            coordinates = geometryFactory.createPoint(Coordinate(candidate.lng, candidate.lat))
            externalIds = providerKey?.let { mapOf(it to context.externalId) } ?: emptyMap()
            importStatus = BuildingImportStatus.COMPLETED
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
            applyCreateDto(dto, candidate, building, providerKey)
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
        entity.applyCreateDto(dto, candidate, building, providerKey)
        entity.restroomMatchKey = restroomMatchKey
        return restroomRepository.saveAndFlush(entity)
    }

    private fun RestroomEntity.applyCreateDto(
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
        phones = dto.phones?.values?.map { it.toString() }
        workTime = dto.workTime
        feeType = dto.feeType ?: feeType
        genderType = dto.genderType ?: genderType
        accessibilityType = dto.accessibilityType
        status = dto.status
        amenities = dto.amenities
        inheritBuildingSchedule = dto.inheritBuildingSchedule
        hasPhotos = dto.hasPhotos
        coordinates = geometryFactory.createPoint(Coordinate(dto.coordinates.lon, dto.coordinates.lat))
        dataSource = DataSourceType.IMPORT
        locationType = dto.locationType
        originProvider = dto.originProvider
        originId = dto.originId
        isHidden = false
        hiddenReason = null
        hiddenAt = null
        externalMaps = mergeProviderMap(externalMaps, providerKey, candidate.providerObjectId)
        subwayStation = null
        accessNote = dto.accessNote
        directionGuide = dto.directionGuide
    }

    private fun mergeExternalMaps(entity: RestroomEntity, providerKey: String, externalId: String) {
        entity.externalMaps = mergeProviderMap(entity.externalMaps, providerKey, externalId)
    }

    private fun mergeExternalIds(entity: BuildingEntity, providerKey: String, externalId: String) {
        val merged = (entity.externalIds ?: emptyMap()).toMutableMap()
        merged.putIfAbsent(providerKey, externalId)
        entity.externalIds = merged
    }

    private fun mergeProviderMap(
        current: Map<String, Any>?,
        providerKey: String?,
        externalId: String
    ): Map<String, Any> {
        if (providerKey == null) {
            return current ?: emptyMap()
        }
        val merged = (current ?: emptyMap()).toMutableMap()
        merged[providerKey] = externalId
        return merged
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
            WHERE is_deleted = false
              AND city_id = ?
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

    private companion object {
        val geometryFactory = GeometryFactory()
    }
}
