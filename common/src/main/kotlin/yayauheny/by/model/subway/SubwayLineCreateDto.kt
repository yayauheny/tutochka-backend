package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID




@Schema(description = "Data for creating a subway line")
data class SubwayLineCreateDto(
    @field:Schema(description = "City ID", required = true)
    
    val cityId: UUID,
    @field:Schema(description = "Russian name", required = true)
    val nameRu: String,
    @field:Schema(description = "English name", required = true)
    val nameEn: String,
    @field:Schema(description = "Stable line code", required = true)
    val code: String,
    @field:Schema(description = "Hex color code", example = "#FF0000", required = true)
    val hexColor: String
)
