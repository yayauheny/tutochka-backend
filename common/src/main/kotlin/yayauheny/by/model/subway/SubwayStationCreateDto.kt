package yayauheny.by.model.subway



import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID


import yayauheny.by.model.dto.Coordinates


@Schema(description = "Data for creating a subway station")
data class SubwayStationCreateDto(
    @field:Schema(description = "Subway line ID", required = true)
    
    val subwayLineId: UUID,
    @field:Schema(description = "Russian name", required = true)
    val nameRu: String,
    @field:Schema(description = "English name", required = true)
    val nameEn: String,
    @field:Schema(description = "Line order within the subway line", required = true)
    val lineOrder: Int,
    @field:Schema(description = "Stable station slug", required = true)
    val slug: String,
    @field:Schema(description = "Is transfer station", required = false)
    val isTransfer: Boolean = false,
    @field:Schema(description = "Coordinates", required = true)
    val coordinates: Coordinates
)
