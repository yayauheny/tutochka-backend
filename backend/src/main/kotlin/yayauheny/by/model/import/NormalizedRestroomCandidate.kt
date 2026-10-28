package yayauheny.by.model.imports

import java.util.UUID
import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus

data class BuildingContext(
    val name: String?,
    val address: String,
    val workTime: JsonNode?,
    val externalId: String
)

/**
 * Каноническая модель для нормализованных данных о туалете перед записью в БД.
 * Используется для унификации данных от разных провайдеров (2ГИС, Яндекс и т.д.).
 * При locationType == INSIDE_BUILDING поле buildingContext заполняется данными здания (название, адрес, расписание, внешний ID) для последующего создания/поиска здания при импорте.
 */
data class NormalizedRestroomCandidate(
    val provider: ImportProvider,
    val providerObjectId: String,
    val cityId: UUID,
    val name: String?,
    val address: String?,
    val lat: Double,
    val lng: Double,
    val placeType: PlaceType,
    val locationType: LocationType,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val status: RestroomStatus,
    val amenities: JsonNode?,
    val rawSchedule: JsonNode? = null,
    val buildingContext: BuildingContext? = null,
    val genderType: GenderType? = null,
    val phones: Map<String, Any>? = null,
    val accessNote: String? = null,
    val directionGuide: String? = null
) {
    fun toCoordinates(): Coordinates = Coordinates(lat = lat, lon = lng)
}
