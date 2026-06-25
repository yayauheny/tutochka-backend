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
}
