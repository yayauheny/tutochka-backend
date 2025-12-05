package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Subway line response DTO")
data class SubwayLineResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID,
    val nameRu: String,
    val nameEn: String,
    val hexColor: String,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant
)
