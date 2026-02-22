package yayauheny.by.model.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.PlaceType

@Serializable
data class NearestRestroomResponseDto(
    @Contextual val id: UUID,
    val name: String?,
    val address: String,
    val coordinates: Coordinates,
    val distanceMeters: Double,
    val feeType: FeeType,
    val isOpen: Boolean? = null,
    val placeType: PlaceType? = null,
    val building: BuildingResponseDto? = null,
    val subwayStation: SubwayStationResponseDto? = null
)
