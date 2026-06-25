package yayauheny.by.cache

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.cache.nearest.NearestRestroomCacheStore
import yayauheny.by.cache.detail.RestroomDetailCacheStore
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class RestroomCacheServiceImpl(
    private val properties: RestroomCacheProperties,
    private val nearestCacheStore: NearestRestroomCacheStore,
    private val detailCacheStore: RestroomDetailCacheStore
) : RestroomCacheService {
    override fun getRestroomById(id: UUID, loader: () -> RestroomResponseDto?): RestroomResponseDto? {
        return runCatching { detailCacheStore.get(id) }.getOrNull()
            ?: loader()?.also { refreshRestroom(it) }
    }

    override fun findNearestRestrooms(
        latitude: Double,
        longitude: Double,
        limit: Int,
        distanceMeters: Int,
        loader: () -> List<NearestRestroomSlimDto>
    ): List<NearestRestroomSlimDto> {
        val normalizedLimit = limit.coerceAtLeast(1).coerceAtMost(properties.nearest.maxResultLimit)
        val normalizedDistanceMeters = distanceMeters.coerceAtMost(properties.nearest.maxSearchRadiusMeters)
        val candidateLimit = candidateLimit(normalizedLimit)

        val cached =
            runCatching {
                nearestCacheStore.read(
                    latitude = latitude,
                    longitude = longitude,
                    distanceMeters = normalizedDistanceMeters,
                    candidateLimit = candidateLimit
                )
            }.getOrElse { emptyList() }

        if (cached.isNotEmpty()) {
            return cached
                .map { it.toNearestRestroomSlim(latitude, longitude) }
                .sortedBy { it.distanceMeters }
                .take(normalizedLimit)
        }

        if (!properties.nearest.fallbackToDatabase) {
            return emptyList()
        }

        val loaded = loader()
        if (loaded.isNotEmpty()) {
            runCatching {
                nearestCacheStore.save(
                    loaded.mapNotNull {
                        it.toCacheItem()
                    }
                )
            }
        }
        return loaded.take(normalizedLimit)
    }

    override fun refreshRestroom(restroom: RestroomResponseDto) {
        runCatching {
            detailCacheStore.put(restroom)
        }

        val cacheItem = restroom.toNearestRestroomCacheItem()
        runCatching {
            if (cacheItem != null) {
                nearestCacheStore.save(listOf(cacheItem))
            } else {
                nearestCacheStore.remove(restroom.id)
            }
        }
    }

    override fun removeRestroom(restroomId: UUID) {
        runCatching {
            detailCacheStore.remove(restroomId)
        }
        runCatching {
            nearestCacheStore.remove(restroomId)
        }
    }

    override fun rebuildNearestCache(items: Collection<NearestRestroomCacheItem>) {
        if (items.size < properties.nearest.minReadyItems) {
            return
        }
        runCatching {
            nearestCacheStore.rebuild(items)
        }
    }

    private fun candidateLimit(limit: Int): Int {
        val multiplied = limit * properties.nearest.candidateLimitMultiplier.coerceAtLeast(1)
        return multiplied.coerceAtLeast(limit).coerceAtMost(properties.nearest.maxCandidateLimit)
    }

    private fun NearestRestroomSlimDto.toCacheItem(): NearestRestroomCacheItem? {
        return NearestRestroomCacheItem(
            id = id,
            lat = restroomCoordinates.lat,
            lon = restroomCoordinates.lon,
            displayName = displayName,
            feeType = feeType
        )
    }
}
