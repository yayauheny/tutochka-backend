package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Data for creating a subway line")
data class SubwayLineCreateDto(
    @field:Schema(description = "City ID", required = true)
    @Contextual
    val cityId: UUID,
    @field:Schema(description = "Russian name", required = true)
    val nameRu: String,
    @field:Schema(description = "English name", required = true)
    val nameEn: String,
    @field:Schema(description = "Local name (e.g., Belarusian, Kazakh)", required = false)
    val nameLocal: String? = null,
    @field:Schema(description = "Local language code (e.g., 'be', 'kk')", required = false)
    val nameLocalLang: String? = null,
    @field:Schema(description = "Short code for the line (e.g., '1', '2', 'M1')", required = false)
    val shortCode: String? = null,
    @field:Schema(description = "Hex color code", example = "#FF0000", required = true)
    val hexColor: String
)
