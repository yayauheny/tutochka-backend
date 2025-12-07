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
    val nameLocal: String? = null,
    val nameLocalLang: String? = null,
    val isTransfer: Boolean = false,
    val coordinates: LatLon,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    val line: SubwayLineResponseDto? = null
) {
    /**
     * Returns display name based on preferred language.
     * Priority: preferredLang -> nameLocal -> nameRu -> nameEn
     */
    fun displayName(preferredLang: String? = null): String =
        when (preferredLang) {
            nameLocalLang -> nameLocal?.takeIf { it.isNotBlank() } ?: nameRu ?: nameEn
            "ru" -> nameRu.takeIf { it.isNotBlank() } ?: nameLocal ?: nameEn
            "en" -> nameEn.takeIf { it.isNotBlank() } ?: nameLocal ?: nameRu
            else -> nameLocal?.takeIf { it.isNotBlank() } ?: nameRu ?: nameEn
        }

    fun lineColor(): String? = line?.hexColor?.takeIf { it.isNotBlank() }
}
