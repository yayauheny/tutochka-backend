package yayauheny.by.bot.cache

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import com.github.benmanes.caffeine.cache.Ticker
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.bot.metrics.BotMetrics
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.NearestRestroomSlimDto

class BackListSnapshotCacheTest {
    @Test
    fun `store returns snapshot and counts hit`() {
        val clock = TestClock(Instant.parse("2025-01-01T00:00:00Z"))
        val registry = SimpleMeterRegistry()
        val cache =
            BackListSnapshotCache.forTest(
                BotMetrics(registry),
                Ticker { TimeUnit.MILLISECONDS.toNanos(clock.millis()) },
                clock,
                Duration.ofMinutes(5),
                10
            )

        val items = listOf(sampleItem("A"))
        cache.store(10L, 20L, 500, items)

        val snapshot = cache.get(10L, 20L)

        assertThat(snapshot).isPresent
        assertThat(snapshot.get().radiusMeters).isEqualTo(500)
        assertThat(snapshot.get().items).hasSize(1)
        assertThat(snapshot.get().items[0].displayName).isEqualTo("A")
        assertThat(registry.get("bot_back_list_cache_total").tag("outcome", "hit").counter().count()).isEqualTo(1.0)
    }

    @Test
    fun `expired snapshot counts expired`() {
        val clock = TestClock(Instant.parse("2025-01-01T00:00:00Z"))
        val registry = SimpleMeterRegistry()
        val cache =
            BackListSnapshotCache.forTest(
                BotMetrics(registry),
                Ticker { TimeUnit.MILLISECONDS.toNanos(clock.millis()) },
                clock,
                Duration.ofMinutes(5),
                10
            )

        cache.store(10L, 20L, 500, listOf(sampleItem("A")))
        clock.advance(Duration.ofMinutes(6))

        val snapshot = cache.get(10L, 20L)

        assertThat(snapshot).isEmpty
        assertThat(registry.get("bot_back_list_cache_total").tag("outcome", "expired").counter().count()).isEqualTo(1.0)
    }

    @Test
    fun `store overwrites previous snapshot`() {
        val clock = TestClock(Instant.parse("2025-01-01T00:00:00Z"))
        val registry = SimpleMeterRegistry()
        val cache =
            BackListSnapshotCache.forTest(
                BotMetrics(registry),
                Ticker { TimeUnit.MILLISECONDS.toNanos(clock.millis()) },
                clock,
                Duration.ofMinutes(5),
                10
            )

        cache.store(10L, 20L, 500, listOf(sampleItem("A")))
        cache.store(10L, 20L, 1000, listOf(sampleItem("B"), sampleItem("C")))

        val snapshot = cache.get(10L, 20L)

        assertThat(snapshot).isPresent
        assertThat(snapshot.get().radiusMeters).isEqualTo(1000)
        assertThat(snapshot.get().items).hasSize(2)
        assertThat(snapshot.get().items[0].displayName).isEqualTo("B")
    }

    @Test
    fun `store empty items does not create snapshot`() {
        val clock = TestClock(Instant.parse("2025-01-01T00:00:00Z"))
        val registry = SimpleMeterRegistry()
        val cache =
            BackListSnapshotCache.forTest(
                BotMetrics(registry),
                Ticker { TimeUnit.MILLISECONDS.toNanos(clock.millis()) },
                clock,
                Duration.ofMinutes(5),
                10
            )

        cache.store(10L, 20L, 500, emptyList())

        assertThat(cache.get(10L, 20L)).isEmpty
        assertThat(registry.get("bot_back_list_cache_total").tag("outcome", "miss").counter().count()).isEqualTo(1.0)
    }

    private fun sampleItem(name: String): NearestRestroomSlimDto =
        NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            123.0,
            FeeType.FREE,
            Coordinates(53.9, 27.56),
            Coordinates(53.9001, 27.5601)
        )

    private class TestClock(private var instant: Instant) : Clock() {
        fun advance(duration: Duration) {
            instant = instant.plus(duration)
        }

        override fun getZone(): ZoneId = ZoneId.of("UTC")

        override fun withZone(zone: ZoneId): Clock = this

        override fun instant(): Instant = instant
    }
}
