package yayauheny.by.model.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.UUID
import yayauheny.by.model.enums.FeeType

/**
 * Slim DTO for nearest restrooms list.
 * Contains only fields necessary for list UI and caching.
 */
@Serializable
data class NearestRestroomSlimDto(
    @Contextual val id: UUID,
    /**
     * Restroom name from DB. May be empty; client (e.g. bot) should show fallback (e.g. "Туалет") when blank.
     */
    val displayName: String,
    val distanceMeters: Double,
    val feeType: FeeType,
    val coordinates: Coordinates,
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
    /**
     * Localized display name (preferred: ru, fallback: en/local)
     */
    val displayName: String,
    /**
     * Line color hex code for emoji mapping
     */
    val lineColorHex: String? = null
)
