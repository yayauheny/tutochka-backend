package yayauheny.by.model.country

import io.swagger.v3.oas.annotations.media.Schema



@Schema(description = "Data for updating an existing country")
data class CountryUpdateDto(
    @field:Schema(description = "Country name in Russian", example = "Соединенные Штаты", required = true)
    val nameRu: String,
    @field:Schema(description = "Country name in English", example = "United States", required = true)
    val nameEn: String
)
