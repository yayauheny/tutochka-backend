package yayauheny.by.model.city

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID


import yayauheny.by.model.dto.Coordinates
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size


@Schema(description = "Data for creating a new city")
data class CityCreateDto(
    @field:Schema(description = "Country ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    
    val countryId: UUID,
    @field:Schema(description = "City name in Russian", example = "Москва", required = true)
    @field:NotBlank @field:Size(min = 1, max = 100)
    val nameRu: String,
    @field:Schema(description = "City name in English", example = "Moscow", required = true)
    @field:NotBlank @field:Size(min = 1, max = 100)
    val nameEn: String,
    @field:Schema(description = "Region/state", example = "Moscow Oblast")
    @field:Size(min = 1, max = 100)
    val region: String? = null,
    @field:Schema(description = "Coordinates", example = "55.7558, 37.6176", required = true)
    @field:Valid
    val coordinates: Coordinates
)
