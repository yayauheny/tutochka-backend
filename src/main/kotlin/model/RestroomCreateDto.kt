package yayauheny.by.model

import kotlinx.serialization.json.JsonObject
import model.enums.AccessibilityType
import model.enums.DataSourceType
import model.enums.FeeType
import java.util.UUID

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
