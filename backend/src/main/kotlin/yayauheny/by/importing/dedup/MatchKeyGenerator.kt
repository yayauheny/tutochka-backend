package yayauheny.by.importing.dedup

import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import yayauheny.by.model.enums.LocationType

private val punctuationRegex = Regex("[\"'`´.,;:()\\[\\]{}<>«»]+")
private val whitespaceRegex = Regex("\\s+")

object MatchKeyGenerator {
    fun normalizeText(value: String?): String? {
        val sanitized =
            value
                ?.trim()
                ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                ?.lowercase(Locale.ROOT)
                ?.replace('ё', 'е')
                ?.replace(punctuationRegex, " ")
                ?.replace(whitespaceRegex, " ")
                ?.trim()
        return sanitized?.takeIf { it.isNotEmpty() }
    }

    fun buildingMatchKey(
        cityId: UUID,
        address: String?,
        lat: Double?,
        lon: Double?
    ): String? {
        val normalizedAddress = normalizeText(address) ?: return null
        val roundedLat = lat?.let(::roundedCoordinate) ?: return null
        val roundedLon = lon?.let(::roundedCoordinate) ?: return null
        return sha256Hex(listOf(cityId.toString(), normalizedAddress, roundedLat, roundedLon).joinToString("|"))
    }

    fun restroomMatchKey(
        cityId: UUID,
        buildingId: UUID?,
        address: String?,
        name: String?,
        lat: Double?,
        lon: Double?,
        locationType: LocationType
    ): String? {
        val roundedLat = lat?.let(::roundedCoordinate) ?: return null
        val roundedLon = lon?.let(::roundedCoordinate) ?: return null
        val normalizedName = normalizeText(name).orEmpty()

        return if (locationType == LocationType.INSIDE_BUILDING) {
            val resolvedBuildingId = buildingId ?: return null
            sha256Hex(
                listOf(
                    resolvedBuildingId.toString(),
                    normalizedName,
                    roundedLat,
                    roundedLon,
                    locationType.name
                ).joinToString("|")
            )
        } else {
            val normalizedAddress = normalizeText(address) ?: return null
            sha256Hex(
                listOf(
                    cityId.toString(),
                    normalizedAddress,
                    normalizedName,
                    roundedLat,
                    roundedLon,
                    locationType.name
                ).joinToString("|")
            )
        }
    }

    private fun roundedCoordinate(value: Double): String = String.format(Locale.US, "%.4f", value)

    private fun sha256Hex(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
