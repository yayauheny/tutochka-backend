package yayauheny.by.model.country

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

import io.swagger.v3.oas.annotations.media.Schema



@Schema(description = "Data for creating a new country")
data class CountryCreateDto(
    @field:Schema(description = "Country name in Russian", example = "Соединенные Штаты", required = true)
    @field:NotBlank @field:Size(min = 1, max = 100)
    val nameRu: String,
    @field:Schema(description = "Country name in English", example = "United States", required = true)
    @field:NotBlank @field:Size(min = 1, max = 100)
    val nameEn: String,
    @field:Schema(description = "Country code", example = "US", required = true)
    @field:NotBlank @field:Size(min = 2, max = 10)
    val code: String
)
