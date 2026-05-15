package yayauheny.by.unit.analytics.service

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsTrend
import yayauheny.by.analytics.repository.PublicAnalyticsRepository
import yayauheny.by.analytics.service.PublicAnalyticsService
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@DisplayName("PublicAnalyticsService Tests")
class PublicAnalyticsServiceTest {
    private val zoneId = ZoneId.of("Europe/Minsk")
    private val fixedClock = Clock.fixed(Instant.parse("2026-05-15T09:00:00Z"), ZoneOffset.UTC)
    private val today = fixedClock.instant().atZone(zoneId).toLocalDate()

    @Test
    @DisplayName("GIVEN live aggregates WHEN building public analytics response THEN compute summary, deltas, and safe series")
    fun given_live_aggregates_when_building_public_analytics_response_then_compute_summary_deltas_and_safe_series() =
        runTest {
            val todayWindow = dayWindow(today)
            val yesterdayWindow = dayWindow(today.minusDays(1))
            val last7DaysWindow = rollingWindow(today, 7)
            val last30DaysWindow = rollingWindow(today, 30)
            val previous30DaysWindow = previousRollingWindow(today, 30)

            val repository =
                FakePublicAnalyticsRepository(
                    totalUsers = 250,
                    newUsersByWindow =
                        mapOf(
                            todayWindow to 2,
                            last7DaysWindow to 11,
                            last30DaysWindow to 40,
                            previous30DaysWindow to 20
                        ),
                    activeUsersByWindow =
                        mapOf(
                            todayWindow to 18,
                            yesterdayWindow to 12,
                            last30DaysWindow to 110,
                            previous30DaysWindow to 90
                        ),
                    metricsByWindow =
                        mapOf(
                            null to PublicAnalyticsEventMetrics(40, 30, 10, 120, 15, 12),
                            todayWindow to PublicAnalyticsEventMetrics(8, 6, 2, 22, 2, 3),
                            last7DaysWindow to PublicAnalyticsEventMetrics(21, 16, 5, 61, 5, 6),
                            last30DaysWindow to PublicAnalyticsEventMetrics(28, 21, 7, 84, 9, 8),
                            previous30DaysWindow to PublicAnalyticsEventMetrics(20, 10, 10, 40, 3, 4)
                        ),
                    dailySeries =
                        listOf(
                            PublicAnalyticsDailyAggregate(today.minusDays(2).toString(), 7, 9, 7, 2, 1, 2),
                            PublicAnalyticsDailyAggregate(today.toString(), 18, 8, 6, 2, 2, 3)
                        ),
                    monthlySeries =
                        listOf(
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).minusMonths(5).toString(), 0, 0, 0),
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).minusMonths(4).toString(), 0, 0, 0),
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).minusMonths(3).toString(), 0, 0, 0),
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).minusMonths(2).toString(), 0, 0, 0),
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).minusMonths(1).toString(), 14, 38, 19),
                            PublicAnalyticsMonthlyAggregate(YearMonth.from(today).toString(), 26, 54, 28)
                        ),
                    eventTotals =
                        listOf(
                            PublicAnalyticsEventCount(ProductAnalyticsEvent.ROUTE_CLICKED.value, 15),
                            PublicAnalyticsEventCount(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value, 12)
                        )
                )

            val service = PublicAnalyticsService(repository, fixedClock)

            val response = service.getPublicAnalytics()

            assertEquals("Europe/Minsk", response.timezone)
            assertEquals(250, response.summary.totalUsers)
            assertEquals(2, response.summary.newUsersToday)
            assertEquals(11, response.summary.newUsers7d)
            assertEquals(40, response.summary.newUsers30d)
            assertEquals(18, response.summary.dailyActiveUsers)
            assertEquals(110, response.summary.monthlyActiveUsers)
            assertEquals(40, response.summary.totalSearches)
            assertEquals(8, response.summary.searchesToday)
            assertEquals(21, response.summary.searches7d)
            assertEquals(28, response.summary.searches30d)
            assertEquals(30, response.summary.successfulSearches)
            assertEquals(10, response.summary.emptySearches)
            assertEquals(75.0, response.summary.successRate)
            assertEquals(3.0, response.summary.averageResultsPerSearch)
            assertEquals(15, response.summary.routeClicks)
            assertEquals(12, response.summary.detailsOpened)

            val newUsersKpi = response.headlineKpis.first { it.key == "new_users_30d" }
            assertEquals(100.0, newUsersKpi.changePct)
            assertEquals(PublicAnalyticsTrend.UP, newUsersKpi.trend)

            val successRateKpi = response.headlineKpis.first { it.key == "success_rate" }
            assertEquals(50.0, successRateKpi.changePct)
            assertEquals(PublicAnalyticsTrend.UP, successRateKpi.trend)

            assertEquals(30, response.dailySeries.size)
            assertEquals(0, response.dailySeries.first().searches)
            assertEquals(8, response.dailySeries.last().searches)
            assertEquals(6, response.monthlySeries.size)
            assertEquals("Route opened", response.eventTotals.first().label)
        }

    private fun rollingWindow(
        anchorDate: LocalDate,
        days: Long
    ): AnalyticsWindow = dateWindow(anchorDate.minusDays(days - 1), anchorDate.plusDays(1))

    private fun previousRollingWindow(
        anchorDate: LocalDate,
        days: Long
    ): AnalyticsWindow = dateWindow(anchorDate.minusDays((days * 2) - 1), anchorDate.minusDays(days - 1))

    private fun dayWindow(date: LocalDate): AnalyticsWindow = dateWindow(date, date.plusDays(1))

    private fun dateWindow(
        startDate: LocalDate,
        endDateExclusive: LocalDate
    ): AnalyticsWindow =
        AnalyticsWindow(
            start = startDate.atStartOfDay(zoneId).toInstant(),
            end = endDateExclusive.atStartOfDay(zoneId).toInstant()
        )

    private class FakePublicAnalyticsRepository(
        private val totalUsers: Long,
        private val newUsersByWindow: Map<AnalyticsWindow, Long>,
        private val activeUsersByWindow: Map<AnalyticsWindow, Long>,
        private val metricsByWindow: Map<AnalyticsWindow?, PublicAnalyticsEventMetrics>,
        private val dailySeries: List<PublicAnalyticsDailyAggregate>,
        private val monthlySeries: List<PublicAnalyticsMonthlyAggregate>,
        private val eventTotals: List<PublicAnalyticsEventCount>
    ) : PublicAnalyticsRepository {
        override suspend fun countTotalUsers(): Long = totalUsers

        override suspend fun countNewUsers(window: AnalyticsWindow): Long = newUsersByWindow[window] ?: 0

        override suspend fun countActiveUsers(window: AnalyticsWindow): Long = activeUsersByWindow[window] ?: 0

        override suspend fun aggregateEventMetrics(window: AnalyticsWindow?): PublicAnalyticsEventMetrics =
            metricsByWindow[window] ?: PublicAnalyticsEventMetrics()

        override suspend fun fetchDailySeries(
            window: AnalyticsWindow,
            zoneId: ZoneId
        ): List<PublicAnalyticsDailyAggregate> = dailySeries

        override suspend fun fetchMonthlySeries(
            startMonth: YearMonth,
            monthsCount: Int,
            zoneId: ZoneId
        ): List<PublicAnalyticsMonthlyAggregate> = monthlySeries

        override suspend fun fetchEventTotals(minimumCount: Long): List<PublicAnalyticsEventCount> = eventTotals
    }
}
