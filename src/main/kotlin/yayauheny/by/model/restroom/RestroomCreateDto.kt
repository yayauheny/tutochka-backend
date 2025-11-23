package yayauheny.by.model.restroom

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.LatLon
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

@Serializable
@Schema(description = "Data for creating a new restroom")
data class RestroomCreateDto(
    @field:Schema(
        description = "City ID where the restroom is located",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val cityId: UUID?,
    @field:Schema(description = "Restroom status", example = "ACTIVE", required = true)
    val status: RestroomStatus,
    @field:Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    val name: String?,
    @field:Schema(
        description = "Detailed description of the restroom",
        example = "Clean public restroom with baby changing facilities"
    )
    val description: String?,
    @field:Schema(
        description = "Street address",
        example = "123 Main Street, Downtown",
        required = true
    )
    val address: String,
    @field:Schema(
        description = "Contact phone numbers in JSON format",
        example = """{"main": "+1-234-567-8900", "emergency": "+1-234-567-8901"}"""
    )
    val phones: JsonObject?,
    @field:Schema(
        description = "Working hours in JSON format",
        example = """{"monday": "08:0-22:00", "tuesday": "08:00-22:00", "weekend": "09:00-21:00"}"""
    )
    val workTime: JsonObject?,
    @field:Schema(description = "Fee type", example = "FREE", required = true)
    val feeType: FeeType,
    @field:Schema(description = "Accessibility type", example = "UNISEX", required = true)
    val accessibilityType: AccessibilityType,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    val coordinates: LatLon,
    @field:Schema(description = "Data source type", example = "MANUAL", required = true)
    val dataSource: DataSourceType,
    @field:Schema(
        description = "Available amenities in JSON format",
        example = """{"wifi": true, "babyChanging": false, "wheelchair": true}""",
        required = true
    )
    val amenities: JsonObject,
    @field:Schema(
        description = "Parent place name (e.g., shopping mall, restaurant)",
        example = "Central Park Mall"
    )
    val parentPlaceName: String? = null,
    @field:Schema(description = "Parent place type", example = "SHOPPING_MALL")
    val parentPlaceType: String? = null,
    @field:Schema(description = "Whether to inherit parent place schedule", example = "false")
    val inheritParentSchedule: Boolean = false
)
