package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Latitude and longitude coordinates")
data class LatLon(
    @field:Schema(description = "Latitude coordinate", example = "55.7558", required = true)
    val lat: Double,
    @field:Schema(description = "Longitude coordinate", example = "37.6176", required = true)
    val lon: Double
)
