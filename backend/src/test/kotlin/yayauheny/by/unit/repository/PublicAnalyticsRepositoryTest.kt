package yayauheny.by.unit.repository

import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.repository.PublicAnalyticsRepository
import yayauheny.by.helpers.DatabaseTestHelper

class PublicAnalyticsRepositoryTest : RepositorySliceTestBase() {
    @Autowired
    private lateinit var repository: PublicAnalyticsRepository

    @Test
    fun given_windowed_users_and_events_when_querying_metrics_then_return_expected_counts() {
        val inWindowUserId = DatabaseTestHelper.insertAnalyticsUser(
            DatabaseTestHelper.createTestAnalyticsUserData(
                createdAt = Instant.parse("2026-05-05T10:00:00Z"),
                updatedAt = Instant.parse("2026-05-05T10:00:00Z")
            )
        )
        val outOfWindowUserId = DatabaseTestHelper.insertAnalyticsUser(
            DatabaseTestHelper.createTestAnalyticsUserData(
                createdAt = Instant.parse("2026-04-01T10:00:00Z"),
                updatedAt = Instant.parse("2026-04-01T10:00:00Z")
            )
        )

        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "nearest_restrooms_returned",
                userId = inWindowUserId,
                resultsCount = 3,
                createdAt = Instant.parse("2026-05-06T09:00:00Z")
            )
        )
        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "nearest_restrooms_no_results",
                userId = inWindowUserId,
                createdAt = Instant.parse("2026-05-06T10:00:00Z")
            )
        )
        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "restroom_details_opened",
                userId = outOfWindowUserId,
                createdAt = Instant.parse("2026-05-06T11:00:00Z")
            )
        )

        val window = AnalyticsWindow(
            start = Instant.parse("2026-05-01T00:00:00Z"),
            end = Instant.parse("2026-05-31T23:59:59Z")
        )

        assertThat(repository.countTotalUsers()).isEqualTo(2)
        assertThat(repository.countNewUsers(window)).isEqualTo(1)
        assertThat(repository.countActiveUsers(window)).isEqualTo(2)

        val metrics = repository.aggregateEventMetrics(window)

        assertThat(metrics.totalSearches).isEqualTo(2)
        assertThat(metrics.successfulSearches).isEqualTo(1)
        assertThat(metrics.emptySearches).isEqualTo(1)
        assertThat(metrics.totalResultsAcrossSearches).isEqualTo(3)
        assertThat(metrics.detailsOpened).isEqualTo(1)
    }

    @Test
    fun given_daily_monthly_and_total_queries_when_fetching_series_then_fill_expected_buckets() {
        val firstUserId = DatabaseTestHelper.insertAnalyticsUser(
            DatabaseTestHelper.createTestAnalyticsUserData(
                createdAt = Instant.parse("2026-04-10T08:00:00Z"),
                updatedAt = Instant.parse("2026-04-10T08:00:00Z")
            )
        )
        val secondUserId = DatabaseTestHelper.insertAnalyticsUser(
            DatabaseTestHelper.createTestAnalyticsUserData(
                createdAt = Instant.parse("2026-05-15T08:00:00Z"),
                updatedAt = Instant.parse("2026-05-15T08:00:00Z")
            )
        )

        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "nearest_restrooms_returned",
                userId = firstUserId,
                resultsCount = 2,
                createdAt = Instant.parse("2026-05-20T10:00:00Z")
            )
        )
        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "restroom_details_opened",
                userId = secondUserId,
                createdAt = Instant.parse("2026-05-20T11:00:00Z")
            )
        )
        DatabaseTestHelper.insertAnalyticsEvent(
            DatabaseTestHelper.createTestAnalyticsEventData(
                event = "nearest_restrooms_no_results",
                userId = secondUserId,
                createdAt = Instant.parse("2026-06-02T09:00:00Z")
            )
        )

        val dailyWindow = AnalyticsWindow(
            start = Instant.parse("2026-05-20T00:00:00Z"),
            end = Instant.parse("2026-05-21T00:00:00Z")
        )

        val dailySeries = repository.fetchDailySeries(dailyWindow, ZoneOffset.UTC)
        assertThat(dailySeries)
            .extracting(
                { it.date },
                { it.activeUsers },
                { it.searches },
                { it.successfulSearches },
                { it.emptySearches },
                { it.detailsOpened }
            )
            .containsExactly(tuple("2026-05-20", 2L, 1L, 1L, 0L, 1L))

        val monthlySeries = repository.fetchMonthlySeries(YearMonth.of(2026, 4), 3, ZoneOffset.UTC)
        assertThat(monthlySeries)
            .extracting({ it.month }, { it.newUsers }, { it.activeUsers }, { it.searches })
            .containsExactly(
                tuple("2026-04", 1L, 0L, 0L),
                tuple("2026-05", 1L, 2L, 1L),
                tuple("2026-06", 0L, 1L, 1L)
            )

        val eventTotals = repository.fetchEventTotals()
        assertThat(eventTotals.map { it.eventKey to it.count }).contains(
            "nearest_restrooms_returned" to 1L,
            "nearest_restrooms_no_results" to 1L,
            "restroom_details_opened" to 1L
        )
    }
}
