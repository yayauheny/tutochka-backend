package by.yayauheny.shared.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.UUID

@Serializable
data class SubwayStationResponseDto(
    @Contextual val id: UUID,
    @Contextual val subwayLineId: UUID,
    val nameRu: String,
    val nameEn: String,
    val coordinates: LatLon,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    val line: SubwayLineResponseDto? = null
) {
    fun displayName(): String? =
        nameRu.takeIf { it.isNotBlank() }
            ?: nameEn.takeIf { it.isNotBlank() }

    fun lineColor(): String? = line?.hexColor?.takeIf { it.isNotBlank() }
}
