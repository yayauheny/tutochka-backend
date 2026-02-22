package yayauheny.by.service.import

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.ImportProvider.GOOGLE_MAPS
import yayauheny.by.model.enums.ImportProvider.OSM
import yayauheny.by.model.enums.ImportProvider.TWO_GIS
import yayauheny.by.model.enums.ImportProvider.YANDEX_MAPS
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.model.restroom.RestroomCreateDto

/**
 * Маппер для преобразования NormalizedRestroomCandidate в RestroomCreateDto.
 */
object RestroomCandidateMapper {
    fun toCreateDto(
        candidate: NormalizedRestroomCandidate,
        genderType: GenderType = GenderType.UNKNOWN
    ): RestroomCreateDto {
        val externalMaps = buildExternalMaps(candidate.provider, candidate.providerObjectId)

        return RestroomCreateDto(
            cityId = candidate.cityId,
            buildingId = null,
            subwayStationId = null,
            status = candidate.status,
            name = candidate.name,
            address = candidate.address,
            phones = null,
            workTime = candidate.rawSchedule,
            feeType = candidate.feeType,
            genderType = genderType,
            accessibilityType = candidate.accessibilityType,
            placeType = candidate.placeType,
            coordinates = candidate.toCoordinates(),
            dataSource = DataSourceType.IMPORT,
            amenities = candidate.amenities,
            externalMaps = externalMaps,
            accessNote = null,
            directionGuide = null,
            inheritBuildingSchedule = false,
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
    ): JsonObject {
        val providerKey =
            when (provider) {
                TWO_GIS -> "2gis"
                YANDEX_MAPS -> "yandex"
                GOOGLE_MAPS -> "google"
                OSM -> "osm"
                ImportProvider.USER, ImportProvider.MANUAL -> null
            }
        return if (providerKey != null) {
            buildJsonObject { put(providerKey, JsonPrimitive(providerObjectId)) }
        } else {
            buildJsonObject { }
        }
    }
}
