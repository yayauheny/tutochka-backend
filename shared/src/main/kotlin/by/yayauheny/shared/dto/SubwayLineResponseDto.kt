package by.yayauheny.shared.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class SubwayLineResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID,
    val nameRu: String,
    val nameEn: String,
    val hexColor: String,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant
)
