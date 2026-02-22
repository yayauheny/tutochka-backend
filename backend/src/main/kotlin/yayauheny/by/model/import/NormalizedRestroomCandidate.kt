package yayauheny.by.model.import

import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType

/**
 * Каноническая модель для нормализованных данных о туалете перед записью в БД.
 * Используется для унификации данных от разных провайдеров (2ГИС, Яндекс и т.д.).
 */
@Serializable
data class NormalizedRestroomCandidate(
    val provider: ImportProvider,
    @Contextual val providerObjectId: String,
    @Contextual val cityId: UUID,
    val name: String?,
    val address: String,
    val lat: Double,
    val lng: Double,
    val placeType: PlaceType,
    val locationType: LocationType,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val rawSchedule: JsonObject? = null
) {
    fun toCoordinates(): Coordinates = Coordinates(lat = lat, lon = lng)
}
