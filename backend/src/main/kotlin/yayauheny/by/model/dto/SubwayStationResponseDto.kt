package yayauheny.by.model.subway

import java.time.Instant
import java.util.Locale
import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.dto.Coordinates

@Serializable
data class SubwayStationResponseDto(
    @Contextual val id: UUID,
    @Contextual val subwayLineId: UUID,
    val nameRu: String,
    val nameEn: String,
    val isTransfer: Boolean = false,
    val coordinates: Coordinates,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    val line: SubwayLineResponseDto? = null
) {
    fun displayName(preferredLang: Locale? = null): String {
        return when (preferredLang) {
            Locale.ENGLISH -> nameEn
            else -> nameRu
        }
    }
}
