package yayauheny.by.cache.nearest

import java.util.UUID

interface NearestRestroomCacheStore {
    fun read(
        latitude: Double,
        longitude: Double,
        distanceMeters: Int,
        candidateLimit: Int
    ): List<NearestRestroomCacheItem>

    fun save(items: Collection<NearestRestroomCacheItem>)

    fun beginRebuild(): String?

    fun appendRebuild(version: String, items: Collection<NearestRestroomCacheItem>)

    fun finishRebuild(version: String, itemCount: Long)

    fun abortRebuild(version: String)

    fun rebuild(items: Collection<NearestRestroomCacheItem>)

    fun remove(restroomId: UUID)
}
