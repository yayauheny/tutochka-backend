package yayauheny.by.cache

import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun RestroomResponseDto.toNearestRestroomCacheItem(): NearestRestroomCacheItem? {
    if (isHidden) {
        return null
    }
    if (status != RestroomStatus.ACTIVE) {
        return null
    }

    val restroomCoordinates = coordinates
    val displayName =
        name?.trim()?.takeIf { it.isNotEmpty() }
            ?: address?.trim()?.takeIf { it.isNotEmpty() }
            ?: "Unknown"

    return NearestRestroomCacheItem(
        id = id,
        lat = restroomCoordinates.lat,
        lon = restroomCoordinates.lon,
        displayName = displayName,
        feeType = feeType ?: FeeType.UNKNOWN
    )
}

fun NearestRestroomCacheItem.toNearestRestroomSlim(
    latitude: Double,
    longitude: Double
): NearestRestroomSlimDto {
    val restroomCoordinates = Coordinates(lat = lat, lon = lon)
    return NearestRestroomSlimDto(
        id = id,
        displayName = displayName,
        distanceMeters = haversineMeters(latitude, longitude, lat, lon),
        feeType = feeType,
        queryCoordinates = Coordinates(lat = latitude, lon = longitude),
        restroomCoordinates = restroomCoordinates
    )
}

private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6_371_000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a =
        sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c
}
