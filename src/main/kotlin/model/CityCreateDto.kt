package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Data for creating a new city")
data class CityCreateDto(
    @Schema(description = "Country ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    val countryId: UUID,
    @Schema(description = "City name in Russian", example = "Москва", required = true)
    val nameRu: String,
    @Schema(description = "City name in English", example = "Moscow", required = true)
    val nameEn: String,
    @Schema(description = "Region/state", example = "Moscow Oblast")
    val region: String? = null,
    @Schema(description = "Latitude", example = "55.7558", required = true)
    val lat: Double,
    @Schema(description = "Longitude", example = "37.6176", required = true)
    val lon: Double
)
