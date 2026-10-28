package yayauheny.by.model.subway

import java.time.Instant
import java.util.UUID
import kotlin.jvm.JvmOverloads


import yayauheny.by.model.dto.Coordinates


data class SubwayStationResponseDto(
     val id: UUID,
     val subwayLineId: UUID?,
    val lineOrder: Int,
    val slug: String,
    val nameRu: String,
    val nameEn: String,
    val isTransfer: Boolean = false,
    val coordinates: Coordinates,
     val createdAt: Instant,
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
