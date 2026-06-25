package yayauheny.by.cache.nearest

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.domain.Pageable
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import yayauheny.by.cache.RestroomCacheProperties
import java.time.Instant
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class RedisNearestRestroomCacheStore(
    private val properties: RestroomCacheProperties,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper
) : NearestRestroomCacheStore {
    override fun read(
        latitude: Double,
        longitude: Double,
        distanceMeters: Int,
        candidateLimit: Int
    ): List<NearestRestroomCacheItem> {
        val version = activeVersion() ?: return emptyList()
        val geoResults =
            redisTemplate.opsForGeo().radius(
                geoKey(version),
                Circle(Point(longitude, latitude), Distance(distanceMeters / 1000.0, Metrics.KILOMETERS)),
                RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                    .sortAscending()
                    .limit(candidateLimit.toLong())
            ) ?: return emptyList()

        val ids = geoResults.content.mapNotNull { it.content.name }
        if (ids.isEmpty()) {
            return emptyList()
        }

        val values = redisTemplate.opsForHash<String, String>().multiGet(itemsKey(version), ids)
        return values.mapNotNull { value ->
            value?.let { deserialize(it) }
        }
    }

    override fun save(items: Collection<NearestRestroomCacheItem>) {
        if (items.isEmpty()) {
            return
        }
        val version = activeVersion() ?: createVersion()
        items.forEach { item ->
            writeItem(version, item)
        }
    }

    override fun rebuild(items: Collection<NearestRestroomCacheItem>) {
        if (items.isEmpty()) {
            return
        }

        val previousVersion = activeVersion()
        val nextVersion = createVersion()
        items.forEach { item ->
            writeItem(nextVersion, item)
        }
        writeMeta(nextVersion, items.size.toLong(), "database")
        redisTemplate.opsForValue().set(activeVersionKey(), nextVersion)

        if (previousVersion != null && previousVersion != nextVersion) {
            expireVersion(previousVersion)
        }
    }

    override fun remove(restroomId: UUID) {
        val version = activeVersion() ?: return
        redisTemplate.opsForGeo().remove(geoKey(version), restroomId.toString())
        redisTemplate.opsForHash<String, String>().delete(itemsKey(version), restroomId.toString())
    }

    private fun writeItem(version: String, item: NearestRestroomCacheItem) {
        redisTemplate.opsForGeo().add(geoKey(version), Point(item.lon, item.lat), item.id.toString())
        redisTemplate.opsForHash<String, String>().put(itemsKey(version), item.id.toString(), serialize(item))
    }

    private fun writeMeta(version: String, itemCount: Long, source: String) {
        val metaKey = metaKey(version)
        val now = Instant.now().toString()
        redisTemplate.opsForHash<String, String>().put(metaKey, "itemCount", itemCount.toString())
        redisTemplate.opsForHash<String, String>().put(metaKey, "rebuiltAt", now)
        redisTemplate.opsForHash<String, String>().put(metaKey, "source", source)
    }

    private fun createVersion(): String {
        val version = UUID.randomUUID().toString()
        redisTemplate.opsForValue().set(activeVersionKey(), version)
        return version
    }

    private fun activeVersion(): String? = redisTemplate.opsForValue().get(activeVersionKey())

    private fun activeVersionKey(): String = "${properties.nearest.keyPrefix}:active-version"

    private fun geoKey(version: String): String = "${properties.nearest.keyPrefix}:$version:geo"

    private fun itemsKey(version: String): String = "${properties.nearest.keyPrefix}:$version:items"

    private fun metaKey(version: String): String = "${properties.nearest.keyPrefix}:$version:meta"

    private fun expireVersion(version: String) {
        val ttl = properties.nearest.oldVersionTtl
        redisTemplate.expire(geoKey(version), ttl)
        redisTemplate.expire(itemsKey(version), ttl)
        redisTemplate.expire(metaKey(version), ttl)
    }

    private fun serialize(item: NearestRestroomCacheItem): String = objectMapper.writeValueAsString(item)

    private fun deserialize(value: String): NearestRestroomCacheItem = objectMapper.readValue(value, NearestRestroomCacheItem::class.java)
}
