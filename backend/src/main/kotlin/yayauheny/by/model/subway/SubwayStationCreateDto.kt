package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import by.yayauheny.shared.dto.LatLon

@Serializable
@Schema(description = "Data for creating a subway station")
data class SubwayStationCreateDto(
    @field:Schema(description = "Subway line ID", required = true)
    @Contextual
    val subwayLineId: UUID,
    @field:Schema(description = "Russian name", required = true)
    val nameRu: String,
    @field:Schema(description = "English name", required = true)
    val nameEn: String,
    @field:Schema(description = "Coordinates", required = true)
    val coordinates: LatLon
)
