package yayauheny.by.model

import java.util.UUID
import kotlinx.serialization.json.JsonObject
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.FeeType

data class RestroomCreateDto(
    val cityId: UUID?,
    val code: String,
    val description: String?,
    val name: String?,
    val workTime: String?,
    val feeType: FeeType,
    val accessibilityType: AccessibilityType,
    val lat: Double,
    val lon: Double,
    val dataSource: String,
    val amenities: JsonObject
)