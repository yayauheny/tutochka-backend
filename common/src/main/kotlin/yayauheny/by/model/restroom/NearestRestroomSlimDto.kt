package yayauheny.by.model.restroom

import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType

@Serializable
data class NearestRestroomSlimDto(
    @Contextual val id: UUID,
    val displayName: String,
    val distanceMeters: Double,
    val feeType: FeeType,
    val queryCoordinates: Coordinates,
    val restroomCoordinates: Coordinates
)
