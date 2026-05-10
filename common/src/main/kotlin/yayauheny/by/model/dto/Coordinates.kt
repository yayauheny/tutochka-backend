package yayauheny.by.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
) {
    constructor(lat: Double?, lon: Double?) : this(
        requireNotNull(lat) { "Missing lat in record" },
        requireNotNull(lon) { "Missing lon in record" }
    )
}
