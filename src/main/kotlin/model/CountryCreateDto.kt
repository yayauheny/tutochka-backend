package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Data for creating a new country")
data class CountryCreateDto(
    @Schema(description = "Country name in Russian", example = "Соединенные Штаты", required = true)
    val nameRu: String,
    @Schema(description = "Country name in English", example = "United States", required = true)
    val nameEn: String,
    @Schema(description = "Country code", example = "US", required = true)
    val code: String
)
