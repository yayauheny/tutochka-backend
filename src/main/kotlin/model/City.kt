package yayauheny.by.model

import java.util.UUID

data class City(
    val id: UUID,
    val countryId: UUID,
    val nameRu: String,
    val nameEn: String,
    val region: String? = null,
    val lat: Double,
    val lon: Double
)
