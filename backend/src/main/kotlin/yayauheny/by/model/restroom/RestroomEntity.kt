package yayauheny.by.model.restroom

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.PlaceType
import by.yayauheny.shared.enums.RestroomStatus

data class RestroomEntity(
    val id: UUID,
    val cityId: UUID?,
    val buildingId: UUID?,
    val subwayStationId: UUID?,
    val name: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val placeType: PlaceType?,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val externalMaps: JsonObject?,
    val accessNote: String?,
    val directionGuide: String?,
    val inheritBuildingSchedule: Boolean,
    val hasPhotos: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
