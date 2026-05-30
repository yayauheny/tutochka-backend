package yayauheny.by.model.restroom

import jakarta.validation.Valid
import jakarta.validation.constraints.Size



import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID



import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType


@Schema(description = "Data for creating a new restroom")
data class RestroomCreateDto(
    @field:Schema(
        description = "City ID where the restroom is located",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    
    val cityId: UUID? = null,
    @field:Schema(description = "Building ID if restroom is inside a building")
    
    val buildingId: UUID? = null,
    @field:Schema(description = "Nearest subway station ID if applicable")
    
    val subwayStationId: UUID? = null,
    @field:Schema(description = "Restroom status", example = "ACTIVE", required = true)
    val status: RestroomStatus,
    @field:Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    @field:Size(min = 1, max = 150)
    val name: String? = null,
    @field:Schema(description = "Street address", example = "123 Main Street, Downtown")
    @field:Size(min = 1, max = 250)
    val address: String? = null,
    @field:Schema(
        description = "Contact phone numbers in JSON format",
        example = """{"main": "+1-234-567-8900", "emergency": "+1-234-567-8901"}"""
    )
    val phones: Map<String, Any>? = null,
    @field:Schema(
        description = "Working hours in JSON format",
        example = """{"monday": "08:0-22:00", "tuesday": "08:00-22:00", "weekend": "09:00-21:00"}"""
    )
    val workTime: Map<String, Any>? = null,
    @field:Schema(description = "Fee type", example = "FREE")
    val feeType: FeeType? = null,
    @field:Schema(description = "Gender type", example = "UNISEX")
    val genderType: GenderType? = null,
    @field:Schema(description = "Accessibility type", example = "WHEELCHAIR", required = true)
    val accessibilityType: AccessibilityType,
    @field:Schema(description = "Place type", example = "other")
    val placeType: PlaceType = PlaceType.OTHER,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    @field:Valid
    val coordinates: Coordinates,
    @field:Schema(description = "Data source type", example = "MANUAL", required = true)
    val dataSource: DataSourceType,
    @field:Schema(
        description = "Available amenities in JSON format",
        example = """{"wifi": true, "babyChanging": false, "wheelchair": true}""",
        required = true
    )
    val amenities: Map<String, Any>,
    @field:Schema(description = "External maps links", example = """{"yandex": "...", "google": "..."}""")
    val externalMaps: Map<String, Any>? = null,
    @field:Schema(description = "Access note / restrictions", example = "Only for customers")
    @field:Size(max = 500)
    val accessNote: String? = null,
    @field:Schema(description = "Indoor directions / navigation", example = "2nd floor, left wing")
    @field:Size(max = 500)
    val directionGuide: String? = null,
    @field:Schema(description = "Inherit schedule from building", example = "false")
    val inheritBuildingSchedule: Boolean = false,
    @field:Schema(description = "Has photos", example = "false")
    val hasPhotos: Boolean = false,
    @field:Schema(description = "Toilet context", example = "STANDALONE")
    val locationType: LocationType = LocationType.UNKNOWN,
    @field:Schema(description = "Origin provider", example = "MANUAL")
    val originProvider: ImportProvider = ImportProvider.MANUAL,
    @field:Schema(description = "Origin ID from provider", example = "2gis_12345")
    val originId: String? = null,
    @field:Schema(description = "Is hidden from search", example = "false")
    val isHidden: Boolean = false
)
