package yayauheny.by.model.dto

import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class SubwayLineResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID,
    val nameRu: String,
    val nameEn: String,
    val shortCode: String? = null,
    val hexColor: String,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant
)
