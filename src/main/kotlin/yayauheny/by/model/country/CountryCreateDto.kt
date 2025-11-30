package yayauheny.by.model.country

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Data for creating a new country")
data class CountryCreateDto(
    @field:Schema(description = "Country name in Russian", example = "Соединенные Штаты", required = true)
    val nameRu: String,
    @field:Schema(description = "Country name in English", example = "United States", required = true)
    val nameEn: String,
    @field:Schema(description = "Country code", example = "US", required = true)
    val code: String
)
