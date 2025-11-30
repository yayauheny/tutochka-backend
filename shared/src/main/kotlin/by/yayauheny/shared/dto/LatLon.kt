package by.yayauheny.shared.dto

import kotlinx.serialization.Serializable

@Serializable
data class LatLon(
    val lat: Double,
    val lon: Double
)
