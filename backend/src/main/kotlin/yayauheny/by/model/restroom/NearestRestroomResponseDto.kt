package yayauheny.by.model.restroom

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.LatLon
import by.yayauheny.shared.enums.FeeType

@Serializable
@Schema(description = "Restroom nearest search response data")
data class NearestRestroomResponseDto(
    @field:Schema(
        description = "Unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val id: UUID,
    @field:Schema(description = "Restroom name", example = "Public Restroom at Central Park")
    val name: String?,
    @field:Schema(description = "Street address", example = "123 Main Street, Downtown")
    val address: String,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    val coordinates: LatLon,
    @field:Schema(description = "Distance from search point in meters", example = "150")
    val distanceMeters: Double,
    @field:Schema(description = "Fee type", example = "FREE")
    val feeType: FeeType,
    @field:Schema(description = "Is currently open/accessible", example = "true")
    val isOpen: Boolean?
)
