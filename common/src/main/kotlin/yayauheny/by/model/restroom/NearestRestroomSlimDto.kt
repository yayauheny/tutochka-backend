package yayauheny.by.model.restroom

import java.util.UUID


import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType


data class NearestRestroomSlimDto(
     val id: UUID,
    val displayName: String,
    val distanceMeters: Double,
    val feeType: FeeType,
    val queryCoordinates: Coordinates,
    val restroomCoordinates: Coordinates
)
