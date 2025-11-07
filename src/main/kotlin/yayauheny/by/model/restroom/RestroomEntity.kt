package yayauheny.by.model.restroom

import com.vividsolutions.jts.geom.Point
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

data class RestroomEntity(
    val id: UUID,
    val cityId: UUID?,
    val name: String?,
    val description: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val coordinates: Point,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val parentPlaceName: String?,
    val parentPlaceType: String?,
    val inheritParentSchedule: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
