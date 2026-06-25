package yayauheny.by.cache.nearest

import yayauheny.by.model.enums.FeeType
import java.util.UUID

data class NearestRestroomCacheItem(
    val id: UUID,
    val lat: Double,
    val lon: Double,
    val displayName: String,
    val feeType: FeeType
)
