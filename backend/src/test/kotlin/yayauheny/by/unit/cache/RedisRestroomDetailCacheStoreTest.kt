package yayauheny.by.unit.cache

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import org.springframework.data.redis.core.ZSetOperations
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.cache.detail.RedisRestroomDetailCacheStore
import yayauheny.by.helpers.TestDataHelpers
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

@DisplayName("RedisRestroomDetailCacheStore Tests")
class RedisRestroomDetailCacheStoreTest {
    private val redisTemplate = mockk<StringRedisTemplate>()
    private val hashOps = mockk<HashOperations<String, String, String>>()
    private val zsetOps = mockk<ZSetOperations<String, String>>()
    private val valueOps = mockk<ValueOperations<String, String>>()
    private val clock = Clock.fixed(Instant.parse("2026-06-25T12:00:00Z"), ZoneOffset.UTC)

    private val properties =
        RestroomCacheProperties(
            popularDetails = RestroomCacheProperties.PopularDetails(
                lfuDecayInterval = Duration.ofHours(1),
                lfuDecayFactor = 0.9
            )
        )

    private val store = RedisRestroomDetailCacheStore(properties, redisTemplate, ObjectMapper().findAndRegisterModules(), clock)

    init {
        every { redisTemplate.opsForHash<String, String>() } returns hashOps
        every { redisTemplate.opsForZSet() } returns zsetOps
        every { redisTemplate.opsForValue() } returns valueOps
        every { redisTemplate.expire(any(), any()) } returns true
        every { zsetOps.incrementScore(any(), any(), any()) } returns 1.0
        every { zsetOps.add(any(), any(), any()) } returns true
        every { zsetOps.remove(any(), any()) } returns 1L
        every { zsetOps.zCard(any()) } returns 2L
        every { hashOps.put(any(), any(), any()) } answers { Unit }
        every { hashOps.delete(any(), any()) } returns 1L
    }

    @Test
    fun `put decays popularity scores when the decay window elapsed`() {
        val restroom = TestDataHelpers.createRestroomResponseDto()
        val lastDecayKey = "restrooms:popular:last-decay"
        val scoreKey = "restrooms:popular:scores"

        every { valueOps.get(lastDecayKey) } returns Instant.parse("2026-06-23T12:00:00Z").toString()
        every { valueOps.set(lastDecayKey, any()) } answers { Unit }
        every { zsetOps.range(scoreKey, 0, -1) } returns linkedSetOf("older", "newer")
        every { zsetOps.score(scoreKey, "older") } returns 10.0
        every { zsetOps.score(scoreKey, "newer") } returns 4.0

        store.put(restroom)

        verify { zsetOps.add(scoreKey, "older", 9.0) }
        verify { zsetOps.add(scoreKey, "newer", 3.6) }
        verify { valueOps.set(lastDecayKey, any()) }
        assertThat(restroom.id).isNotNull()
    }
}
