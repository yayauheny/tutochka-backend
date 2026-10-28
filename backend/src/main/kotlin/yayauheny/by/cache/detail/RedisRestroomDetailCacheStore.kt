package yayauheny.by.cache.detail

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import yayauheny.by.cache.RestroomCacheProperties
import yayauheny.by.model.restroom.RestroomResponseDto
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.UUID

@Component
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class RedisRestroomDetailCacheStore(
    private val properties: RestroomCacheProperties,
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    private val clock: Clock
) : RestroomDetailCacheStore {
    override fun get(restroomId: UUID): RestroomResponseDto? {
        decayScoresIfNeeded()
        val json = redisTemplate.opsForHash<String, String>().get(itemsKey(), restroomId.toString()) ?: return null
        redisTemplate.opsForZSet().incrementScore(scoreKey(), restroomId.toString(), 1.0)
        return objectMapper.readValue(json, RestroomResponseDto::class.java)
    }

    override fun put(restroom: RestroomResponseDto) {
        decayScoresIfNeeded()
        val id = restroom.id.toString()
        redisTemplate.opsForHash<String, String>().put(itemsKey(), id, objectMapper.writeValueAsString(restroom))
        redisTemplate.opsForZSet().incrementScore(scoreKey(), id, 1.0)
        evictOverflow()
        touchTtl()
    }

    override fun remove(restroomId: UUID) {
        val id = restroomId.toString()
        redisTemplate.opsForHash<String, String>().delete(itemsKey(), id)
        redisTemplate.opsForZSet().remove(scoreKey(), id)
    }

    private fun evictOverflow() {
        val maxItems = properties.popularDetails.maxItems.coerceAtLeast(1)
        val size = redisTemplate.opsForZSet().zCard(scoreKey()) ?: 0L
        if (size <= maxItems) {
            return
        }

        val overflow = (size - maxItems).coerceAtLeast(1)
        val evicted = redisTemplate.opsForZSet().range(scoreKey(), 0, overflow - 1) ?: emptySet()
        if (evicted.isEmpty()) {
            return
        }

        redisTemplate.opsForZSet().remove(scoreKey(), *evicted.toTypedArray())
        redisTemplate.opsForHash<String, String>().delete(itemsKey(), *evicted.toTypedArray())
    }

    private fun touchTtl() {
        redisTemplate.expire(itemsKey(), properties.popularDetails.detailTtl)
        redisTemplate.expire(scoreKey(), properties.popularDetails.scoreTtl)
    }

    private fun decayScoresIfNeeded() {
        if (!properties.popularDetails.algorithm.equals("lfu", ignoreCase = true)) {
            return
        }

        val now = clock.instant()
        val lastDecay = redisTemplate.opsForValue().get(lastDecayKey())?.let { runCatching { Instant.parse(it) }.getOrNull() }
        if (lastDecay == null) {
            redisTemplate.opsForValue().set(lastDecayKey(), now.toString())
            return
        }
        if (Duration.between(lastDecay, now) < properties.popularDetails.lfuDecayInterval) {
            return
        }

        val members = redisTemplate.opsForZSet().range(scoreKey(), 0, -1).orEmpty()
        if (members.isNotEmpty()) {
            members.forEach { member ->
                val currentScore = redisTemplate.opsForZSet().score(scoreKey(), member) ?: return@forEach
                redisTemplate.opsForZSet().add(scoreKey(), member, currentScore * properties.popularDetails.lfuDecayFactor)
            }
        }
        redisTemplate.opsForValue().set(lastDecayKey(), now.toString())
    }

    private fun itemsKey(): String = "${properties.popularDetails.keyPrefix}:items"

    private fun scoreKey(): String = "${properties.popularDetails.keyPrefix}:scores"

    private fun lastDecayKey(): String = "${properties.popularDetails.keyPrefix}:last-decay"
}
