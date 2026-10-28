package yayauheny.by.unit.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.connection.RedisGeoCommands
import org.springframework.data.redis.core.GeoOperations
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisOperations
import org.springframework.data.redis.core.SessionCallback
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.cache.nearest.RedisNearestRestroomCacheStore
import yayauheny.by.model.enums.FeeType
import java.util.UUID

@DisplayName("RedisNearestRestroomCacheStore Tests")
class RedisNearestRestroomCacheStoreTest {
    private val redisTemplate = mockk<StringRedisTemplate>()
    private val valueOps = mockk<ValueOperations<String, String>>()
    private val hashOps = mockk<HashOperations<String, String, String>>()
    private val geoOps = mockk<GeoOperations<String, String>>()
    private val redisOperations = mockk<RedisOperations<String, String>>()
    private val store = RedisNearestRestroomCacheStore(RestroomCacheProperties(), redisTemplate, com.fasterxml.jackson.databind.ObjectMapper())

    init {
        every { redisTemplate.opsForValue() } returns valueOps
        every { redisTemplate.opsForHash<String, String>() } returns hashOps
        every { redisTemplate.opsForGeo() } returns geoOps
        every { redisTemplate.executePipelined(any<SessionCallback<Any?>>()) } answers {
            val callback = firstArg<SessionCallback<Any?>>()
            every { redisOperations.opsForGeo() } returns geoOps
            every { redisOperations.opsForHash<String, String>() } returns hashOps
            callback.execute(redisOperations)
            emptyList()
        }
        every { valueOps.set(any(), any()) } answers { Unit }
        every { geoOps.add(any(), any(), any()) } returns 1L
        every { geoOps.remove(any(), any()) } returns 1L
        every { hashOps.put(any(), any(), any()) } answers { Unit }
        every { hashOps.delete(any(), any()) } returns 1L
    }

    @Test
    fun `save writes to active and rebuild versions when rebuild is running`() {
        val item = item()
        every { valueOps.get("restrooms:nearest:active-version") } returns "active-v1"
        every { valueOps.get("restrooms:nearest:rebuild-lock") } returns "pending-v2"

        store.save(listOf(item))

        verify(exactly = 2) { redisTemplate.executePipelined(any<SessionCallback<Any?>>()) }
        verify { geoOps.add("restrooms:nearest:active-v1:geo", any(), item.id.toString()) }
        verify { geoOps.add("restrooms:nearest:pending-v2:geo", any(), item.id.toString()) }
        verify { hashOps.put("restrooms:nearest:active-v1:items", item.id.toString(), any()) }
        verify { hashOps.put("restrooms:nearest:pending-v2:items", item.id.toString(), any()) }
    }

    @Test
    fun `save creates active version only after writing batch`() {
        val item = item()
        every { valueOps.get("restrooms:nearest:active-version") } returns null
        every { valueOps.get("restrooms:nearest:rebuild-lock") } returns null

        store.save(listOf(item))

        verifyOrder {
            redisTemplate.executePipelined(any<SessionCallback<Any?>>())
            valueOps.set("restrooms:nearest:active-version", any())
        }
    }

    private fun item() = NearestRestroomCacheItem(
        id = UUID.randomUUID(),
        lat = 53.9,
        lon = 27.56,
        displayName = "Test restroom",
        feeType = FeeType.FREE
    )
}
