package yayauheny.by.unit.metrics

import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.config.BackendMetrics

@DisplayName("BackendMetrics — unit tests")
class BackendMetricsTest {
    private val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    private val metrics = BackendMetrics(registry)

    @BeforeEach
    fun setUp() {
        registry.clear()
    }

    private fun scrape(): String = registry.scrape()

    @Nested
    @DisplayName("Nearest search — outcome counters")
    inner class NearestSearchOutcome {
        @Test
        fun `GIVEN search returns results WHEN record found THEN counter with outcome=found increments`() {
            metrics.recordNearestSearchFound()
            metrics.recordNearestSearchFound()

            val output = scrape()

            assertThat(output)
                .`as`("Nearest search 'found' counter should be 2.0")
                .contains("restroom_nearest_search_total{outcome=\"found\",} 2.0")
        }

        @Test
        fun `GIVEN search returns empty WHEN record not found THEN counter with outcome=not_found increments`() {
            metrics.recordNearestSearchNotFound()

            val output = scrape()

            assertThat(output)
                .`as`("Nearest search 'not_found' counter should be 1.0")
                .contains("restroom_nearest_search_total{outcome=\"not_found\",} 1.0")
        }

        @Test
        fun `GIVEN search throws exception WHEN record error THEN counter with outcome=error increments`() {
            metrics.recordNearestSearchError()
            metrics.recordNearestSearchError()
            metrics.recordNearestSearchError()

            val output = scrape()

            assertThat(output)
                .`as`("Nearest search 'error' counter should be 3.0")
                .contains("restroom_nearest_search_total{outcome=\"error\",} 3.0")
        }
    }

    @Nested
    @DisplayName("Nearest search — duration timer")
    inner class NearestSearchDuration {
        @Test
        fun `GIVEN search duration recorded WHEN scrape THEN histogram metric exists`() {
            metrics.recordNearestSearchDuration(0.042)

            val output = scrape()

            assertThat(output)
                .`as`("Should contain nearest search duration histogram")
                .contains("restroom_nearest_search_duration_seconds")
        }

        @Test
        fun `GIVEN two searches WHEN both durations recorded THEN count is 2`() {
            metrics.recordNearestSearchDuration(0.01)
            metrics.recordNearestSearchDuration(0.05)

            val output = scrape()

            assertThat(output)
                .`as`("Duration count should be 2.0")
                .contains("restroom_nearest_search_duration_seconds_count 2.0")
        }
    }

    @Nested
    @DisplayName("City restroom requests")
    inner class CityRestroomRequests {
        @Test
        fun `GIVEN request for Minsk WHEN recorded THEN counter with city=Minsk increments`() {
            metrics.recordCityRestroomRequest("Minsk")

            val output = scrape()

            assertThat(output)
                .`as`("City request counter for Minsk should be 1.0")
                .contains("restroom_by_city_requests_total{city=\"Minsk\",} 1.0")
        }

        @Test
        fun `GIVEN requests for different cities WHEN recorded THEN each city tracked separately`() {
            metrics.recordCityRestroomRequest("Minsk")
            metrics.recordCityRestroomRequest("Minsk")
            metrics.recordCityRestroomRequest("Moscow")

            val output = scrape()

            assertThat(output)
                .`as`("Minsk counter should be 2.0")
                .contains("restroom_by_city_requests_total{city=\"Minsk\",} 2.0")
            assertThat(output)
                .`as`("Moscow counter should be 1.0")
                .contains("restroom_by_city_requests_total{city=\"Moscow\",} 1.0")
        }
    }

    @Nested
    @DisplayName("Combined scenario")
    inner class CombinedScenario {
        @Test
        fun `GIVEN mixed operations WHEN all recorded THEN all expected metrics present`() {
            metrics.recordNearestSearchFound()
            metrics.recordNearestSearchNotFound()
            metrics.recordNearestSearchError()
            metrics.recordNearestSearchDuration(0.1)
            metrics.recordCityRestroomRequest("Minsk")

            val output = scrape()

            assertThat(output).contains("restroom_nearest_search_total{outcome=\"found\",} 1.0")
            assertThat(output).contains("restroom_nearest_search_total{outcome=\"not_found\",} 1.0")
            assertThat(output).contains("restroom_nearest_search_total{outcome=\"error\",} 1.0")
            assertThat(output).contains("restroom_nearest_search_duration_seconds")
            assertThat(output).contains("restroom_by_city_requests_total{city=\"Minsk\",} 1.0")
        }
    }
}
