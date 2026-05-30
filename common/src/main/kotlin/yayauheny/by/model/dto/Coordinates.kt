package yayauheny.by.model.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min




data class Coordinates(
    @field:Min(-90) @field:Max(90)
    val lat: Double,
    @field:Min(-180) @field:Max(180)
    val lon: Double
) {
    constructor(lat: Double?, lon: Double?) : this(
        requireNotNull(lat) { "Missing lat in record" },
        requireNotNull(lon) { "Missing lon in record" }
    )
}
