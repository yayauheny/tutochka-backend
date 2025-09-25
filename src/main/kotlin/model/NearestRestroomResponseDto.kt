package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus

@Serializable
@Schema(description = "Restroom nearest search response data")
data class NearestRestroomResponseDto(
    @Schema(description = "Unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual val id: UUID,
    @Schema(description = "City ID where the restroom is located", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual val cityId: UUID?,
    @Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    val name: String?,
    @Schema(description = "Detailed description of the restroom", example = "Clean public restroom with baby changing facilities")
    val description: String?,
    @Schema(description = "Street address", example = "123 Main Street, Downtown")
    val address: String,
    @Schema(description = "Contact phone numbers in JSON format")
    val phones: JsonObject?,
    @Schema(description = "Working hours in JSON format")
    val workTime: JsonObject?,
    @Schema(description = "Fee type", example = "FREE")
    val feeType: FeeType,
    @Schema(description = "Accessibility type", example = "UNISEX")
    val accessibilityType: AccessibilityType,
    @Schema(description = "Latitude coordinate", example = "55.7558")
    val lat: Double,
    @Schema(description = "Longitude coordinate", example = "37.6176")
    val lon: Double,
    @Schema(description = "Data source type", example = "MANUAL")
    val dataSource: DataSourceType,
    @Schema(description = "Current status of the restroom", example = "ACTIVE")
    val status: RestroomStatus,
    @Schema(description = "Available amenities in JSON format")
    val amenities: JsonObject,
    @Schema(description = "Parent place name (e.g., shopping mall, restaurant)", example = "Central Park Mall")
    val parentPlaceName: String?,
    @Schema(description = "Parent place type", example = "SHOPPING_MALL")
    val parentPlaceType: String?,
    @Schema(description = "Whether to inherit parent place schedule", example = "false")
    val inheritParentSchedule: Boolean,
    @Schema(description = "Creation timestamp", example = "2023-12-01T10:30:00Z")
    @Contextual val createdAt: Instant,
    @Schema(description = "Last update timestamp", example = "2023-12-01T15:45:00Z")
    @Contextual val updatedAt: Instant,
    @Schema(description = "Distance from search point in meters", example = "150")
    val distanceMeters: Int
)
