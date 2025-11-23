package yayauheny.by.model.city

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.LatLon

@Serializable
@Schema(description = "Data for updating an existing city")
data class CityUpdateDto(
    @field:Schema(
        description = "Country ID",
        example = "123e4567-e89b-12d3-a456-42661417400",
        required = true
    )
    @Contextual
    val countryId: UUID,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    val coordinates: LatLon,
    @field:Schema(
        description = "City boundary as Polygon",
        example = """{"type":"Polygon","coordinates":[[[37.3,55.5],[37.9,55.5],[37.9,55.9],[37.3,55.9],[37.3,55.5]]]}""",
        nullable = true
    )
    val cityBounds: String?,
    @field:Schema(description = "City name in Russian", example = "Москва", required = true)
    val nameRu: String,
    @field:Schema(description = "City name in English", example = "Moscow", required = true)
    val nameEn: String,
    @field:Schema(description = "Region/state", example = "Moscow Oblast")
    val region: String? = null
)
