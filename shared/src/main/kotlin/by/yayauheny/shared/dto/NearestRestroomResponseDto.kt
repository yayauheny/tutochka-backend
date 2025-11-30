package by.yayauheny.shared.dto

import by.yayauheny.shared.enums.FeeType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class NearestRestroomResponseDto(
    @Contextual val id: UUID,
    val name: String?,
    val address: String,
    val coordinates: LatLon,
    val distanceMeters: Double,
    val feeType: FeeType,
    val isOpen: Boolean? = null
)
