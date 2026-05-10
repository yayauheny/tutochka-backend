package yayauheny.by.model.restroom

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType

@Serializable
@Schema(description = "Data for updating an existing restroom")
data class RestroomUpdateDto(
    @field:Schema(
        description = "City ID where the restroom is located",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val cityId: UUID?,
    @field:Schema(description = "Building ID if restroom is inside a building")
    @Contextual
    val buildingId: UUID?,
    @field:Schema(description = "Nearest subway station ID if applicable")
    @Contextual
    val subwayStationId: UUID?,
    @field:Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    val name: String?,
    @field:Schema(description = "Street address", example = "123 Main Street, Downtown")
    val address: String? = null,
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
    val feeType: FeeType? = null,
    @field:Schema(description = "Gender type", example = "UNISEX")
    val genderType: GenderType? = null,
    @field:Schema(description = "Accessibility type", example = "WHEELCHAIR")
    val accessibilityType: AccessibilityType?,
    @field:Schema(description = "Place type", example = "public_toilet")
    val placeType: PlaceType?,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    val coordinates: Coordinates,
    @field:Schema(description = "Current status of the restroom", example = "ACTIVE")
    val status: RestroomStatus,
    @field:Schema(
        description = "Available amenities in JSON format",
        example = """{"wifi": true, "babyChanging": false, "wheelchair": true}"""
    )
    val amenities: JsonObject?,
    @field:Schema(description = "External maps links", example = """{"yandex": "...", "google": "..."}""")
    val externalMaps: JsonObject?,
    @field:Schema(description = "Access note / restrictions", example = "Only for customers")
    val accessNote: String?,
    @field:Schema(description = "Indoor directions / navigation", example = "2nd floor, left wing")
    val directionGuide: String?,
    @field:Schema(description = "Inherit schedule from building", example = "false")
    val inheritBuildingSchedule: Boolean = false,
    @field:Schema(description = "Has photos", example = "false")
    val hasPhotos: Boolean = false,
    @field:Schema(description = "Toilet context", example = "STANDALONE")
    val locationType: LocationType? = null,
    @field:Schema(description = "Origin provider", example = "MANUAL")
    val originProvider: ImportProvider? = null,
    @field:Schema(description = "Origin ID from provider", example = "2gis_12345")
    val originId: String? = null,
    @field:Schema(description = "Is hidden from search", example = "false")
    val isHidden: Boolean? = null
)
