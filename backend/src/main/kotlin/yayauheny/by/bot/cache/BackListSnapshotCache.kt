package yayauheny.by.bot.cache

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import com.github.benmanes.caffeine.cache.RemovalCause
import com.github.benmanes.caffeine.cache.Ticker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import yayauheny.by.bot.metrics.BotMetrics
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Component
class BackListSnapshotCache private constructor(
    private val botMetrics: BotMetrics,
    ticker: Ticker,
    private val clock: Clock,
    private val ttl: Duration,
    maxSize: Long
) {
    companion object {
        private val DEFAULT_TTL = Duration.ofMinutes(5)
        private const val DEFAULT_MAX_SIZE = 5_000L

        fun forTest(
            botMetrics: BotMetrics,
            ticker: Ticker,
            clock: Clock,
            ttl: Duration,
            maxSize: Long
        ): BackListSnapshotCache {
            return BackListSnapshotCache(botMetrics, ticker, clock, ttl, maxSize)
        }
    }

    @Autowired
    constructor(botMetrics: BotMetrics) : this(
        botMetrics,
        Ticker.systemTicker(),
        Clock.systemUTC(),
        DEFAULT_TTL,
        DEFAULT_MAX_SIZE
    )

    private val cache: Cache<CacheKey, BackListSnapshot> = Caffeine.newBuilder()
        .ticker(ticker)
        .expireAfterWrite(ttl)
        .maximumSize(maxSize)
        .removalListener { key: CacheKey?, value: BackListSnapshot?, cause: RemovalCause ->
            onRemoval(key, value, cause)
        }
        .build()

    private val states = ConcurrentHashMap<CacheKey, CacheState>()
    private val sequence = AtomicLong()

    fun store(chatId: Long, userId: Long, radiusMeters: Int, items: List<NearestRestroomSlimDto>?) {
        if (items.isNullOrEmpty()) {
            return
        }

        val key = CacheKey(chatId, userId)
        val snapshotSequence = sequence.incrementAndGet()
        val createdAt = clock.instant()
        val snapshot = BackListSnapshot(
            snapshotSequence,
            chatId,
            userId,
            radiusMeters,
            items.toList(),
            createdAt
        )

        cache.put(key, snapshot)
        states[key] = CacheState.present(snapshotSequence, createdAt.plus(ttl))
    }

    fun get(chatId: Long, userId: Long): Optional<BackListSnapshot> {
        val key = CacheKey(chatId, userId)
        val snapshot = cache.getIfPresent(key)
        if (snapshot != null) {
            botMetrics.incrementBackListCache("hit")
            return Optional.of(snapshot)
        }

        val state = states[key]
        if (state != null && state.isExpired(clock.instant())) {
            botMetrics.incrementBackListCache("expired")
            states.remove(key, state)
            return Optional.empty()
        }

        botMetrics.incrementBackListCache("miss")
        return Optional.empty()
    }

    private fun onRemoval(key: CacheKey?, snapshot: BackListSnapshot?, cause: RemovalCause) {
        if (key == null || snapshot == null) {
            return
        }

        states.computeIfPresent(key) { _, current ->
            if (current.sequence != snapshot.sequence) {
                return@computeIfPresent current
            }
            if (cause == RemovalCause.EXPIRED) {
                return@computeIfPresent current.markExpired()
            }
            null
        }
    }

    data class BackListSnapshot(
        val sequence: Long,
        val chatId: Long,
        val userId: Long,
        val radiusMeters: Int,
        val items: List<NearestRestroomSlimDto>,
        val createdAt: Instant
    )

    private data class CacheKey(val chatId: Long, val userId: Long)

    private data class CacheState(
        val sequence: Long,
        val expiresAt: Instant,
        val present: Boolean,
        val lastCause: RemovalCause?
    ) {
        companion object {
            fun present(sequence: Long, expiresAt: Instant): CacheState {
                return CacheState(sequence, expiresAt, true, null)
            }
        }

        fun markExpired(): CacheState {
            return CacheState(sequence, expiresAt, false, RemovalCause.EXPIRED)
        }

        fun isExpired(now: Instant): Boolean {
            return (present && !now.isBefore(expiresAt)) || lastCause == RemovalCause.EXPIRED
        }
    }
}
