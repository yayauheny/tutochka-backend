package yayauheny.by.cache.nearest

import java.util.UUID

interface NearestRestroomCacheRow {
    val id: UUID
    val lat: Double
    val lon: Double
    val displayName: String
    val feeType: String
}
