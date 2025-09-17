package yayauheny.by.model

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

data class Restroom(
    val id: UUID,
    val cityId: UUID?,
    val name: String?,
    val description: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val coordinates: GeoPoint,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: JsonObject,
    val createdAt: Instant,
    val updatedAt: Instant
)
