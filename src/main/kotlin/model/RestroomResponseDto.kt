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
@Schema(description = "Restroom response data")
data class RestroomResponseDto(
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
    @Schema(
        description = "Contact phone numbers in JSON format",
        example = """{"main": "+1-234-567-8900", "emergency": "+1-234-567-8901"}"""
    )
    val phones: JsonObject?,
    @Schema(
        description = "Working hours in JSON format",
        example = """{"monday": "08:00-22:00", "tuesday": "08:00-22:00", "weekend": "09:00-21:00"}"""
    )
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
    @Schema(description = "Available amenities in JSON format", example = """{"wifi": true, "babyChanging": false, "wheelchair": true}""")
    val amenities: JsonObject,
    @Schema(description = "Creation timestamp", example = "2023-12-01T10:30:00Z")
    @Contextual val createdAt: Instant,
    @Schema(description = "Last update timestamp", example = "2023-12-01T15:45:00Z")
    @Contextual val updatedAt: Instant
)
