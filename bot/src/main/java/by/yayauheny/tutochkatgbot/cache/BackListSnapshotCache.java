package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;

@Component
public class BackListSnapshotCache {
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(5);
    private static final long DEFAULT_MAX_SIZE = 5_000L;

    private final Cache<CacheKey, BackListSnapshot> cache;
    private final ConcurrentHashMap<CacheKey, CacheState> states = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong();
    private final BotMetrics botMetrics;
    private final Clock clock;
    private final Duration ttl;

    @Autowired
    public BackListSnapshotCache(BotMetrics botMetrics) {
        this(botMetrics, Ticker.systemTicker(), Clock.systemUTC(), DEFAULT_TTL, DEFAULT_MAX_SIZE);
    }

    private BackListSnapshotCache(
        BotMetrics botMetrics,
        Ticker ticker,
        Clock clock,
        Duration ttl,
        long maxSize
    ) {
        this.botMetrics = botMetrics;
        this.clock = clock;
        this.ttl = ttl;
        this.cache =
            Caffeine
                .newBuilder()
                .ticker(ticker)
                .expireAfterWrite(ttl)
                .maximumSize(maxSize)
                .removalListener(this::onRemoval)
                .build();
    }

    public void store(long chatId, long userId, int radiusMeters, List<NearestRestroomSlimDto> items) {
        if (items == null || items.isEmpty()) {
            return;
        }

        CacheKey key = new CacheKey(chatId, userId);
        long snapshotSequence = sequence.incrementAndGet();
        Instant createdAt = clock.instant();
        BackListSnapshot snapshot =
            new BackListSnapshot(
                snapshotSequence,
                chatId,
                userId,
                radiusMeters,
                List.copyOf(items),
                createdAt
            );

        cache.put(key, snapshot);
        states.put(key, CacheState.present(snapshotSequence, createdAt.plus(ttl)));
    }

    public static BackListSnapshotCache forTest(
        BotMetrics botMetrics,
        Ticker ticker,
        Clock clock,
        Duration ttl,
        long maxSize
    ) {
        return new BackListSnapshotCache(botMetrics, ticker, clock, ttl, maxSize);
    }

    public Optional<BackListSnapshot> get(long chatId, long userId) {
        CacheKey key = new CacheKey(chatId, userId);
        BackListSnapshot snapshot = cache.getIfPresent(key);
        if (snapshot != null) {
            botMetrics.incrementBackListCache("hit");
            return Optional.of(snapshot);
        }

        CacheState state = states.get(key);
        if (state != null && state.isExpired(clock.instant())) {
            botMetrics.incrementBackListCache("expired");
            states.remove(key, state);
            return Optional.empty();
        }

        botMetrics.incrementBackListCache("miss");
        return Optional.empty();
    }

    private void onRemoval(
        CacheKey key,
        BackListSnapshot snapshot,
        RemovalCause cause
    ) {
        if (key == null || snapshot == null) {
            return;
        }

        states.computeIfPresent(
            key,
            (ignored, current) -> {
                if (current.sequence() != snapshot.sequence()) {
                    return current;
                }
                if (cause == RemovalCause.EXPIRED) {
                    return current.markExpired();
                }
                return null;
            }
        );
    }

    public record BackListSnapshot(
        long sequence,
        long chatId,
        long userId,
        int radiusMeters,
        List<NearestRestroomSlimDto> items,
        Instant createdAt
    ) {}

    private record CacheKey(long chatId, long userId) {}

    private record CacheState(long sequence, Instant expiresAt, boolean present, RemovalCause lastCause) {
        static CacheState present(long sequence, Instant expiresAt) {
            return new CacheState(sequence, expiresAt, true, null);
        }

        CacheState markExpired() {
            return new CacheState(sequence, expiresAt, false, RemovalCause.EXPIRED);
        }

        boolean isExpired(Instant now) {
            return (present && !now.isBefore(expiresAt)) || lastCause == RemovalCause.EXPIRED;
        }
    }
}
