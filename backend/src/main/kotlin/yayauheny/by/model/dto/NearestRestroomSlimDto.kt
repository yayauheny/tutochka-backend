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
    val restroomCoordinates: Coordinates,
    /**
     * Minimal subway station info for list display.
     * Contains only displayName and lineColor for emoji rendering.
     */
    val subwayStation: SubwayStationSlimDto? = null
)

/**
 * Minimal subway station info for list display.
 */
@Serializable
data class SubwayStationSlimDto(
    @Contextual val id: UUID,
    /**
     * Localized display name (preferred: ru, fallback: en/local)
     */
    val displayName: String,
    /**
     * Line color hex code for emoji mapping
     */
    val lineColorHex: String? = null
)
