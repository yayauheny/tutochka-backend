package yayauheny.by.entity

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.GeoPoint

data class Restroom(
    val id: UUID,
    val cityId: UUID?,
    val code: String,
    val description: String?,
    val name: String?,
    val workTime: String?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val coordinates: GeoPoint,
    val dataSource: DataSourceType,
    val amenities: JsonObject,
    val createdAt: Instant,
    val updatedAt: Instant
)
