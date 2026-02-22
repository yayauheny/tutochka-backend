package yayauheny.by.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class Coordinates(
    val lat: Double,
    val lon: Double
)
