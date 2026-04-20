package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackListSnapshotCacheTest {

    @Test
    void store_shouldReturnSnapshotAndCountHit() {
        TestClock clock = new TestClock(Instant.parse("2025-01-01T00:00:00Z"));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BackListSnapshotCache cache =
            BackListSnapshotCache.forTest(
                new BotMetrics(registry),
                () -> TimeUnit.MILLISECONDS.toNanos(clock.millis()),
                clock,
                Duration.ofMinutes(5),
                10
            );

        List<NearestRestroomSlimDto> items = List.of(sampleItem("A"));
        cache.store(10L, 20L, 500, items);

        var snapshot = cache.get(10L, 20L);

        assertTrue(snapshot.isPresent());
        assertEquals(500, snapshot.get().radiusMeters());
        assertEquals(1, snapshot.get().items().size());
        assertEquals("A", snapshot.get().items().get(0).displayName());
        assertEquals(1.0, registry.get("bot_back_list_cache_total").tag("outcome", "hit").counter().count());
    }

    @Test
    void expiredSnapshot_shouldCountExpired() {
        TestClock clock = new TestClock(Instant.parse("2025-01-01T00:00:00Z"));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BackListSnapshotCache cache =
            BackListSnapshotCache.forTest(
                new BotMetrics(registry),
                () -> TimeUnit.MILLISECONDS.toNanos(clock.millis()),
                clock,
                Duration.ofMinutes(5),
                10
            );

        cache.store(10L, 20L, 500, List.of(sampleItem("A")));
        clock.advance(Duration.ofMinutes(6));

        var snapshot = cache.get(10L, 20L);

        assertTrue(snapshot.isEmpty());
        assertEquals(1.0, registry.get("bot_back_list_cache_total").tag("outcome", "expired").counter().count());
    }

    @Test
    void store_shouldOverwritePreviousSnapshot() {
        TestClock clock = new TestClock(Instant.parse("2025-01-01T00:00:00Z"));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BackListSnapshotCache cache =
            BackListSnapshotCache.forTest(
                new BotMetrics(registry),
                () -> TimeUnit.MILLISECONDS.toNanos(clock.millis()),
                clock,
                Duration.ofMinutes(5),
                10
            );

        cache.store(10L, 20L, 500, List.of(sampleItem("A")));
        cache.store(10L, 20L, 1000, List.of(sampleItem("B"), sampleItem("C")));

        var snapshot = cache.get(10L, 20L);

        assertTrue(snapshot.isPresent());
        assertEquals(1000, snapshot.get().radiusMeters());
        assertEquals(2, snapshot.get().items().size());
        assertEquals("B", snapshot.get().items().get(0).displayName());
    }

    @Test
    void storeEmptyItems_shouldNotCreateSnapshot() {
        TestClock clock = new TestClock(Instant.parse("2025-01-01T00:00:00Z"));
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BackListSnapshotCache cache =
            BackListSnapshotCache.forTest(
                new BotMetrics(registry),
                () -> TimeUnit.MILLISECONDS.toNanos(clock.millis()),
                clock,
                Duration.ofMinutes(5),
                10
            );

        cache.store(10L, 20L, 500, List.of());

        assertTrue(cache.get(10L, 20L).isEmpty());
        assertEquals(1.0, registry.get("bot_back_list_cache_total").tag("outcome", "miss").counter().count());
    }

    private NearestRestroomSlimDto sampleItem(String name) {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            123.0,
            FeeType.FREE,
            new LatLon(53.9, 27.56),
            new LatLon(53.9001, 27.5601)
        );
    }

    private static final class TestClock extends Clock {
        private Instant instant;

        private TestClock(Instant instant) {
            this.instant = instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
