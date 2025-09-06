package yayauheny.by.model

import java.util.UUID

data class CityCreateDto(
    val countryId: UUID,
    val nameRu: String,
    val nameEn: String,
    val region: String? = null,
    val lat: Double,
    val lon: Double
)
