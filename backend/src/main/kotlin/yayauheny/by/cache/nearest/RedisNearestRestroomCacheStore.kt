package yayauheny.by.cache.nearest

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.geo.Circle
import org.springframework.data.geo.Distance
import org.springframework.data.geo.Metrics
import org.springframework.data.geo.Point
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import yayauheny.by.cache.RestroomCacheProperties
import java.util.ArrayList
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
        val batch = items.toList()
        if (batch.isEmpty()) {
            return
        }

        val versions = writeTargetVersions()
        if (versions.isEmpty()) {
            val version = UUID.randomUUID().toString()
            writeItems(version, batch)
            redisTemplate.opsForValue().set(activeVersionKey(), version)
            return
        }

        versions.forEach { version ->
            writeItems(version, batch)
        }
    }

    override fun beginRebuild(): String? {
        val version = UUID.randomUUID().toString()
        val acquired = redisTemplate.opsForValue().setIfAbsent(rebuildLockKey(), version, properties.nearest.rebuildLockTtl)
        return if (acquired == true) version else null
    }

    override fun appendRebuild(version: String, items: Collection<NearestRestroomCacheItem>) {
        val batch = items.toList()
        if (batch.isEmpty()) {
            return
        }
        require(rebuildVersion() == version) {
            "Rebuild session is no longer active"
        }
        writeItems(version, batch)
    }

    override fun finishRebuild(version: String, itemCount: Long) {
        if (rebuildVersion() != version) {
            return
        }
        if (itemCount < properties.nearest.minReadyItems) {
            abortRebuild(version)
            return
        }

        val previousVersion = activeVersion()
        writeMeta(version, itemCount, "database")
        redisTemplate.opsForValue().set(activeVersionKey(), version)

        if (previousVersion != null && previousVersion != version) {
            expireVersion(previousVersion)
        }
        clearRebuildLock(version)
    }

    override fun abortRebuild(version: String) {
        if (rebuildVersion() == version) {
            clearRebuildLock(version)
        }
        deleteVersion(version)
    }

    override fun rebuild(items: Collection<NearestRestroomCacheItem>) {
        val batch = items.toList()
        if (batch.isEmpty()) {
            return
        }

        val rebuildVersion = beginRebuild() ?: return
        try {
            batch.chunked(properties.nearest.redisPipelineBatchSize.coerceAtLeast(1)).forEach { chunk ->
                appendRebuild(rebuildVersion, chunk)
            }
            finishRebuild(rebuildVersion, batch.size.toLong())
        } catch (e: RuntimeException) {
            abortRebuild(rebuildVersion)
            throw e
        }
    }

    override fun remove(restroomId: UUID) {
        val versions = writeTargetVersions()
        if (versions.isEmpty()) {
            return
        }

        versions.forEach { version ->
            redisTemplate.opsForGeo().remove(geoKey(version), restroomId.toString())
            redisTemplate.opsForHash<String, String>().delete(itemsKey(version), restroomId.toString())
        }
    }

    private fun writeItems(version: String, items: List<NearestRestroomCacheItem>) {
        items.chunked(properties.nearest.redisPipelineBatchSize.coerceAtLeast(1)).forEach { chunk ->
            redisTemplate.executePipelined(
                object : SessionCallback<Any?> {
                    override fun <K : Any?, V : Any?> execute(operations: RedisOperations<K, V>): Any? {
                        @Suppress("UNCHECKED_CAST")
                        val stringOperations = operations as RedisOperations<String, String>
                        val geoOperations = stringOperations.opsForGeo()
                        val hashOperations = stringOperations.opsForHash<String, String>()
                        chunk.forEach { item ->
                            geoOperations.add(geoKey(version), Point(item.lon, item.lat), item.id.toString())
                            hashOperations.put(itemsKey(version), item.id.toString(), serialize(item))
                        }
                        return null
                    }
                }
            )
        }
    }

    private fun writeMeta(version: String, itemCount: Long, source: String) {
        redisTemplate.opsForHash<String, String>().put(metaKey(version), "itemCount", itemCount.toString())
        redisTemplate.opsForHash<String, String>().put(metaKey(version), "source", source)
    }

    private fun activeVersion(): String? = redisTemplate.opsForValue().get(activeVersionKey())

    private fun rebuildVersion(): String? = redisTemplate.opsForValue().get(rebuildLockKey())

    private fun activeVersionKey(): String = "${properties.nearest.keyPrefix}:active-version"

    private fun rebuildLockKey(): String = "${properties.nearest.keyPrefix}:rebuild-lock"

    private fun geoKey(version: String): String = "${properties.nearest.keyPrefix}:$version:geo"

    private fun itemsKey(version: String): String = "${properties.nearest.keyPrefix}:$version:items"

    private fun metaKey(version: String): String = "${properties.nearest.keyPrefix}:$version:meta"

    private fun expireVersion(version: String) {
        val ttl = properties.nearest.oldVersionTtl
        redisTemplate.expire(geoKey(version), ttl)
        redisTemplate.expire(itemsKey(version), ttl)
        redisTemplate.expire(metaKey(version), ttl)
    }

    private fun deleteVersion(version: String) {
        redisTemplate.delete(listOf(geoKey(version), itemsKey(version), metaKey(version)))
    }

    private fun clearRebuildLock(version: String) {
        if (rebuildVersion() == version) {
            redisTemplate.delete(rebuildLockKey())
        }
    }

    private fun writeTargetVersions(): List<String> {
        val versions = ArrayList<String>(2)
        activeVersion()?.let { versions += it }
        rebuildVersion()?.let { if (it !in versions) versions += it }
        return versions
    }

    private fun serialize(item: NearestRestroomCacheItem): String = objectMapper.writeValueAsString(item)

    private fun deserialize(value: String): NearestRestroomCacheItem = objectMapper.readValue(value, NearestRestroomCacheItem::class.java)
}
