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
     */
    fun displayName(preferredLang: String? = null): String {
        val lang = preferredLang?.lowercase()?.trim()
        val normalizedLocalLang = nameLocalLang?.lowercase()?.trim()

        val candidates =
            when (lang) {
                normalizedLocalLang -> listOf(nameLocal, nameRu, nameEn)
                "ru" -> listOf(nameRu, nameLocal, nameEn)
                "en" -> listOf(nameEn, nameLocal, nameRu)
                else -> listOf(nameLocal, nameEn, nameRu)
            }

        return candidates.firstOrNull { !it.isNullOrBlank() }?.trim() ?: "Unknown"
    }

    fun lineColor(): String? = line?.hexColor?.takeIf { it.isNotBlank() }
}
