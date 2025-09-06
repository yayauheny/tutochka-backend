package yayauheny.by.model

import java.time.Instant
import java.util.UUID

data class CityResponseDto(
    val id: UUID,
    val countryId: UUID,
    val nameRu: String,
    val nameEn: String,
    val region: String? = null,
    val lat: Double,
    val lon: Double,
    val createdAt: Instant,
    val updatedAt: Instant
)
