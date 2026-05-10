package yayauheny.by.model.subway

import java.time.Instant
import java.util.UUID
import kotlin.jvm.JvmOverloads
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.dto.Coordinates

@Serializable
data class SubwayStationResponseDto(
    @Contextual val id: UUID,
    @Contextual val subwayLineId: UUID?,
    val nameRu: String,
    val nameEn: String,
    val isTransfer: Boolean = false,
    val coordinates: Coordinates,
    val isDeleted: Boolean,
    @Contextual val createdAt: Instant,
    val line: SubwayLineResponseDto? = null
) {
    @JvmOverloads
    fun displayName(preferredLang: String? = null): String {
        return when {
            preferredLang?.equals("en", ignoreCase = true) == true && !nameEn.isBlank() -> nameEn.trim()
            !nameRu.isBlank() -> nameRu.trim()
            !nameEn.isBlank() -> nameEn.trim()
            else -> "Unknown"
        }
    }

    fun lineColor(): String? = line?.hexColor?.takeIf { it.isNotBlank() }
}
