package yayauheny.by.model.city

import com.vividsolutions.jts.geom.Point
import com.vividsolutions.jts.geom.Polygon
import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "City response data")
data class CityResponseDto(
    @field:Schema(
        description = "Unique identifier",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    @Contextual
    val id: UUID,
    @field:Schema(
        description = "Geographic coordinates of the city as a Point",
        example = """{"type":"Point","coordinates":[37.6176,55.7558]}""",
        type = "object"
    )
    val coordinates: Point,
    @field:Schema(
        description = "City boundary as Polygon",
        example = """{"type":"Polygon","coordinates":[[[37.3,55.5],[37.9,55.5],[37.9,55.9],[37.3,55.9],[37.3,55.5]]]}""",
        nullable = true
    )
    val cityBounds: Polygon?,
    @field:Schema(description = "Country ID", example = "123e4567-e89b-12d3-a456-426614174000")
    @Contextual
    val countryId: UUID,
    @field:Schema(description = "City name in Russian", example = "Москва")
    val nameRu: String,
    @field:Schema(description = "City name in English", example = "Moscow")
    val nameEn: String,
    @field:Schema(description = "Region/state", example = "Moscow Oblast")
    val region: String? = null
)
