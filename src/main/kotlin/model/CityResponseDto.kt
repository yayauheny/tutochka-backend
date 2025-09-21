package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "City response data")
data class CityResponseDto(
    @Schema(description = "Unique identifier", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual
    val id: UUID,
    @Schema(description = "Country ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual
    val countryId: UUID,
    @Schema(description = "City name in Russian", example = "Москва")
    val nameRu: String,
    @Schema(description = "City name in English", example = "Moscow")
    val nameEn: String,
    @Schema(description = "Region/state", example = "Moscow Oblast")
    val region: String? = null,
    @Schema(description = "Latitude", example = "55.7558")
    val lat: Double,
    @Schema(description = "Longitude", example = "37.6176")
    val lon: Double
)
