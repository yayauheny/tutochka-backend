package yayauheny.by.model.city

import com.vividsolutions.jts.geom.Polygon
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Data for updating an existing city")
data class CityUpdateDto(
    @field:Schema(
        description = "Country ID",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    @Contextual
    val countryId: UUID,
    @field:Schema(description = "Latitude", example = "55.7558", required = true)
    val lat: Double,
    @field:Schema(description = "Longitude", example = "37.6176", required = true)
    val lon: Double,
    @field:Schema(
        description = "City boundary as Polygon",
        example = """{"type":"Polygon","coordinates":[[[37.3,55.5],[37.9,55.5],[37.9,55.9],[37.3,55.9],[37.3,55.5]]]}""",
        nullable = true
    )
    val cityBounds: Polygon?,
    @field:Schema(description = "City name in Russian", example = "Москва", required = true)
    val nameRu: String,
    @field:Schema(description = "City name in English", example = "Moscow", required = true)
    val nameEn: String,
    @field:Schema(description = "Region/state", example = "Moscow Oblast")
    val region: String? = null
)
