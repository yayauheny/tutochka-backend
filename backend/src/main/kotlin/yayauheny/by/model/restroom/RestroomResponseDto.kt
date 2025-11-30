package yayauheny.by.model.restroom

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.RestroomStatus

@Serializable
@Schema(description = "Restroom response data")
data class RestroomResponseDto(
    @field:Schema(
        description = "Unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual val id: UUID,
    @field:Schema(
        description = "City ID where the restroom is located",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual val cityId: UUID?,
    @field:Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    val name: String?,
    @field:Schema(
        description = "Detailed description of the restroom",
        example = "Clean public restroom with baby changing facilities"
    )
    val description: String?,
    @field:Schema(description = "Street address", example = "123 Main Street, Downtown")
    val address: String,
    @field:Schema(
        description = "Contact phone numbers in JSON format",
        example = """{"main": "+1-234-567-8900", "emergency": "+1-234-567-8901"}"""
    )
    val phones: JsonObject?,
    @field:Schema(
        description = "Working hours in JSON format",
        example = """{"monday": "08:00-22:00", "tuesday": "08:00-22:00", "weekend": "09:00-21:00"}"""
    )
    val workTime: JsonObject?,
    @field:Schema(description = "Fee type", example = "FREE")
    val feeType: FeeType,
    @field:Schema(description = "Accessibility type", example = "UNISEX")
    val accessibilityType: AccessibilityType,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    val coordinates: LatLon,
    @field:Schema(description = "Data source type", example = "MANUAL")
    val dataSource: DataSourceType,
    @field:Schema(description = "Current status of the restroom", example = "ACTIVE")
    val status: RestroomStatus,
    @field:Schema(
        description = "Available amenities in JSON format",
        example = """{"wifi": true, "babyChanging": false, "wheelchair": true}"""
    )
    val amenities: JsonObject?,
    @field:Schema(
        description = "Parent place name (e.g., shopping mall, restaurant)",
        example = "Central Park Mall"
    )
    val parentPlaceName: String?,
    @field:Schema(description = "Parent place type", example = "SHOPPING_MALL")
    val parentPlaceType: String?,
    @field:Schema(description = "Whether to inherit parent place schedule", example = "false")
    val inheritParentSchedule: Boolean,
    @field:Schema(description = "Creation timestamp", example = "2023-12-01T10:30:00Z")
    @Contextual val createdAt: Instant,
    @field:Schema(description = "Last update timestamp", example = "2023-12-01T15:45:00Z")
    @Contextual val updatedAt: Instant,
    @field:Schema(
        description = "Distance from search point in meters (only present in nearest search results)",
        example = "150"
    )
    val distanceMeters: Int? = null
)
