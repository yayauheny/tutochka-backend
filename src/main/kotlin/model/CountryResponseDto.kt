package yayauheny.by.model

import java.time.Instant
import java.util.UUID

data class CountryResponseDto(
    val id: UUID,
    val name: String,
    val code: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
