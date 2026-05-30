package yayauheny.by.importing.mapper

import java.util.UUID
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.imports.NormalizedRestroomCandidate
import yayauheny.by.model.restroom.RestroomCreateDto

object RestroomImportMapper {
    private val mapper = jacksonObjectMapper()

    fun toCreateDto(
        candidate: NormalizedRestroomCandidate,
        genderType: GenderType? = null,
        buildingId: UUID? = null,
        inheritBuildingSchedule: Boolean = false
    ): RestroomCreateDto {
        val resolvedGender = candidate.genderType ?: genderType ?: GenderType.UNKNOWN
        val externalMaps = buildExternalMaps(candidate.provider, candidate.providerObjectId)
        val workTime =
            if (inheritBuildingSchedule) {
                null
            } else {
                candidate.rawSchedule
            }

        return RestroomCreateDto(
            cityId = candidate.cityId,
            buildingId = buildingId,
            subwayStationId = null,
            status = candidate.status,
            name = candidate.name?.trim().takeIf { !it.isNullOrBlank() && !it.equals("null", ignoreCase = true) } ?: "Туалет",
            address = candidate.address,
            phones = null,
            workTime = emptyMap(),
            feeType = candidate.feeType,
            genderType = resolvedGender,
            accessibilityType = candidate.accessibilityType,
            placeType = candidate.placeType,
            coordinates = candidate.toCoordinates(),
            dataSource = DataSourceType.IMPORT,
            amenities = emptyMap(),
            externalMaps = emptyMap(),
            accessNote = null,
            directionGuide = null,
            inheritBuildingSchedule = inheritBuildingSchedule,
            hasPhotos = false,
            locationType = candidate.locationType,
            originProvider = candidate.provider,
            originId = candidate.providerObjectId,
            isHidden = false
        )
    }

    private fun buildExternalMaps(
        provider: ImportProvider,
        providerObjectId: String
    ): JsonNode {
        val providerKey =
            when (provider) {
                ImportProvider.TWO_GIS -> "2gis"
                ImportProvider.YANDEX_MAPS -> "yandex"
                ImportProvider.GOOGLE_MAPS -> "google"
                ImportProvider.OSM -> "osm"
                ImportProvider.USER, ImportProvider.MANUAL -> null
            }
        return if (providerKey != null) {
            val node = mapper.createObjectNode()
            node.put(providerKey, providerObjectId)
            node
        } else {
            mapper.createObjectNode()
        }
    }
}
