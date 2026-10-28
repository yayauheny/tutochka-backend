package yayauheny.by.cache

import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

interface RestroomCacheService {
    fun getRestroomById(id: UUID, loader: () -> RestroomResponseDto?): RestroomResponseDto?

    fun findNearestRestrooms(
        latitude: Double,
        longitude: Double,
        limit: Int,
        distanceMeters: Int,
        loader: () -> List<NearestRestroomSlimDto>
    ): List<NearestRestroomSlimDto>

    fun refreshRestroom(restroom: RestroomResponseDto)

    fun removeRestroom(restroomId: UUID)

    fun rebuildNearestCache(items: Collection<NearestRestroomCacheItem>)

    fun beginRebuild(): String?

    fun appendRebuild(version: String, items: Collection<NearestRestroomCacheItem>)

    fun finishRebuild(version: String, itemCount: Long)

    fun abortRebuild(version: String)
}

object NoopRestroomCacheService : RestroomCacheService {
    override fun getRestroomById(id: UUID, loader: () -> RestroomResponseDto?): RestroomResponseDto? = loader()

    override fun findNearestRestrooms(
        latitude: Double,
        longitude: Double,
        limit: Int,
        distanceMeters: Int,
        loader: () -> List<NearestRestroomSlimDto>
    ): List<NearestRestroomSlimDto> = loader()

    override fun refreshRestroom(restroom: RestroomResponseDto) = Unit

    override fun removeRestroom(restroomId: UUID) = Unit

    override fun rebuildNearestCache(items: Collection<NearestRestroomCacheItem>) = Unit

    override fun beginRebuild(): String? = null

    override fun appendRebuild(version: String, items: Collection<NearestRestroomCacheItem>) = Unit

    override fun finishRebuild(version: String, itemCount: Long) = Unit

    override fun abortRebuild(version: String) = Unit
}
