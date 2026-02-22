package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.dto.Coordinates

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
    @field:Schema(description = "Is transfer station", required = false)
    val isTransfer: Boolean = false,
    @field:Schema(description = "External IDs (JSON object)", required = false)
    val externalIds: kotlinx.serialization.json.JsonObject? = null,
    @field:Schema(description = "Coordinates", required = true)
    val coordinates: Coordinates
)
