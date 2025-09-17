package yayauheny.by.model

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType

data class RestroomCreateDto(
    val cityId: UUID?,
    val name: String?,
    val description: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val lat: Double,
    val lon: Double,
    val dataSource: DataSourceType,
    val amenities: JsonObject
)