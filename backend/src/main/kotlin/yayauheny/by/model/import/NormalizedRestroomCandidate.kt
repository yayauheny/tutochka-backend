package yayauheny.by.model.import

import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus

@Serializable
data class BuildingContext(
    val name: String?,
    val address: String,
    val workTime: JsonObject?,
    val externalId: String
)

/**
 * Каноническая модель для нормализованных данных о туалете перед записью в БД.
 * Используется для унификации данных от разных провайдеров (2ГИС, Яндекс и т.д.).
 * При locationType == INSIDE_BUILDING поле buildingContext заполняется данными здания (название, адрес, расписание, внешний ID) для последующего создания/поиска здания при импорте.
 */
@Serializable
data class NormalizedRestroomCandidate(
    val provider: ImportProvider,
    @Contextual val providerObjectId: String,
    @Contextual val cityId: UUID,
    val name: String?,
    val address: String?,
    val lat: Double,
    val lng: Double,
    val placeType: PlaceType,
    val locationType: LocationType,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val rawSchedule: JsonObject? = null,
    val buildingContext: BuildingContext? = null
) {
    fun toCoordinates(): Coordinates = Coordinates(lat = lat, lon = lng)
}
