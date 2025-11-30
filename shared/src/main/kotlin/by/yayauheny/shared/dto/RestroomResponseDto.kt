package by.yayauheny.shared.dto

import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.RestroomStatus
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.util.UUID

@Serializable
data class RestroomResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID?,
    val name: String?,
    val description: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val coordinates: LatLon,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: JsonObject?,
    val parentPlaceName: String?,
    val parentPlaceType: String?,
    val inheritParentSchedule: Boolean,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val distanceMeters: Int? = null
)
