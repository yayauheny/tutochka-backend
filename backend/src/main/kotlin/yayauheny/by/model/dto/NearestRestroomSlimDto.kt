package yayauheny.by.model.dto

import java.util.UUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import yayauheny.by.model.enums.FeeType

/**
 * Slim DTO for nearest restrooms list.
 * Contains only fields necessary for list UI and caching.
 */
@Serializable
data class NearestRestroomSlimDto(
    @Contextual val id: UUID,
    /**
     * Restroom name from DB.
     */
    val displayName: String,
    val distanceMeters: Double,
    val feeType: FeeType,
    val queryCoordinates: Coordinates,
    val restroomCoordinates: Coordinates
)
