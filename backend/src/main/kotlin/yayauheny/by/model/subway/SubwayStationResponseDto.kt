package yayauheny.by.model.subway

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import by.yayauheny.shared.dto.LatLon

@Serializable
@Schema(description = "Subway station response DTO")
data class SubwayStationResponseDto(
    @Contextual val id: UUID,
    @Contextual val subwayLineId: UUID,
    val nameRu: String,
    val nameEn: String,
    val coordinates: LatLon,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant
)
