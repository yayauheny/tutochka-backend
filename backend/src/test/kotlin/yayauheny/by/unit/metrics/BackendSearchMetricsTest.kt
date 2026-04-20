package yayauheny.by.unit.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import yayauheny.by.metrics.BackendSearchMetrics

class BackendSearchMetricsTest {
    private val registry = SimpleMeterRegistry()
    private val metrics = BackendSearchMetrics(registry)

    @Test
    fun `incrementSearchRequest should increase counter`() {
        metrics.incrementSearchRequest("telegram_bot", "0_300")

        val counter =
            registry
                .get("search_requests_total")
                .tag("client_type", "telegram_bot")
                .tag("radius_bucket", "0_300")
                .counter()

        assertEquals(1.0, counter.count())
    }

    @Test
    fun `incrementSearchResults should increase counter`() {
        metrics.incrementSearchResults("telegram_bot", "3_5")

        val counter =
            registry
                .get("search_results_total")
                .tag("client_type", "telegram_bot")
                .tag("result_bucket", "3_5")
                .counter()

        assertEquals(1.0, counter.count())
    }

    @Test
    fun `incrementSearchQuality should increase counter`() {
        metrics.incrementSearchQuality("telegram_bot", "strong")

        val counter =
            registry
                .get("search_quality_total")
                .tag("client_type", "telegram_bot")
                .tag("quality_bucket", "strong")
                .counter()

        assertEquals(1.0, counter.count())
    }

    @Test
    fun `incrementSearchFailure should increase counter`() {
        metrics.incrementSearchFailure("validation")

        val counter =
            registry
                .get("search_failures_total")
                .tag("reason", "validation")
                .counter()

        assertEquals(1.0, counter.count())
    }
}
