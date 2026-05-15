package yayauheny.by.analytics.service

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailySeriesPoint
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsEventTotal
import yayauheny.by.analytics.model.PublicAnalyticsHeadlineKpi
import yayauheny.by.analytics.model.PublicAnalyticsMonthlySeriesPoint
import yayauheny.by.analytics.model.PublicAnalyticsResponse
import yayauheny.by.analytics.model.PublicAnalyticsSummary
import yayauheny.by.analytics.model.PublicAnalyticsTrend
import yayauheny.by.analytics.repository.PublicAnalyticsRepository
import yayauheny.by.model.analytics.ProductAnalyticsEvent

class PublicAnalyticsService(
    private val publicAnalyticsRepository: PublicAnalyticsRepository,
    private val clock: Clock
) {
    private val zoneId = ZoneId.of("Europe/Minsk")

    suspend fun getPublicAnalytics(): PublicAnalyticsResponse {
        val now = Instant.now(clock)
        val today = now.atZone(zoneId).toLocalDate()

        val todayWindow = dayWindow(today)
        val yesterdayWindow = dayWindow(today.minusDays(1))
        val last7DaysWindow = rollingWindow(today, 7)
        val last30DaysWindow = rollingWindow(today, 30)
        val previous30DaysWindow = previousRollingWindow(today, 30)
        val currentMonth = YearMonth.from(today)
        val startMonth = currentMonth.minusMonths(5)

        val totalUsers = publicAnalyticsRepository.countTotalUsers()
        val newUsersToday = publicAnalyticsRepository.countNewUsers(todayWindow)
        val newUsers7d = publicAnalyticsRepository.countNewUsers(last7DaysWindow)
        val newUsers30d = publicAnalyticsRepository.countNewUsers(last30DaysWindow)
        val previousNewUsers30d = publicAnalyticsRepository.countNewUsers(previous30DaysWindow)

        val dailyActiveUsers = publicAnalyticsRepository.countActiveUsers(todayWindow)
        val dailyActiveUsersYesterday = publicAnalyticsRepository.countActiveUsers(yesterdayWindow)
        val monthlyActiveUsers = publicAnalyticsRepository.countActiveUsers(last30DaysWindow)
        val previousMonthlyActiveUsers = publicAnalyticsRepository.countActiveUsers(previous30DaysWindow)

        val lifetimeMetrics = publicAnalyticsRepository.aggregateEventMetrics()
        val todayMetrics = publicAnalyticsRepository.aggregateEventMetrics(todayWindow)
        val last7DaysMetrics = publicAnalyticsRepository.aggregateEventMetrics(last7DaysWindow)
        val last30DaysMetrics = publicAnalyticsRepository.aggregateEventMetrics(last30DaysWindow)
        val previous30DaysMetrics = publicAnalyticsRepository.aggregateEventMetrics(previous30DaysWindow)

        val dailySeries =
            fillDailySeries(
                startDate = today.minusDays(29),
                points =
                    publicAnalyticsRepository
                        .fetchDailySeries(last30DaysWindow, zoneId)
            ).map { point ->
                PublicAnalyticsDailySeriesPoint(
                    date = point.date,
                    activeUsers = point.activeUsers,
                    searches = point.searches,
                    successfulSearches = point.successfulSearches,
                    emptySearches = point.emptySearches,
                    routeClicks = point.routeClicks,
                    detailsOpened = point.detailsOpened
                )
            }

        val monthlySeries =
            publicAnalyticsRepository
                .fetchMonthlySeries(startMonth, 6, zoneId)
                .map { point ->
                    PublicAnalyticsMonthlySeriesPoint(
                        month = point.month,
                        newUsers = point.newUsers,
                        activeUsers = point.activeUsers,
                        searches = point.searches
                    )
                }

        val eventTotals =
            publicAnalyticsRepository
                .fetchEventTotals(minimumCount = 10)
                .mapNotNull { eventCount ->
                    eventLabel(eventCount.eventKey)?.let { label ->
                        PublicAnalyticsEventTotal(
                            eventKey = eventCount.eventKey,
                            label = label,
                            count = eventCount.count
                        )
                    }
                }

        return PublicAnalyticsResponse(
            generatedAt = now,
            timezone = zoneId.id,
            headlineKpis =
                listOf(
                    buildHeadlineKpi(
                        "new_users_30d",
                        "New users",
                        newUsers30d.toDouble(),
                        "Last 30 days",
                        newUsers30d.toDouble(),
                        previousNewUsers30d.toDouble()
                    ),
                    buildHeadlineKpi(
                        "searches_30d",
                        "Toilet searches",
                        last30DaysMetrics.totalSearches.toDouble(),
                        "Last 30 days",
                        last30DaysMetrics.totalSearches.toDouble(),
                        previous30DaysMetrics.totalSearches.toDouble()
                    ),
                    buildHeadlineKpi(
                        "daily_active_users",
                        "Daily active users",
                        dailyActiveUsers.toDouble(),
                        "Today",
                        dailyActiveUsers.toDouble(),
                        dailyActiveUsersYesterday.toDouble()
                    ),
                    buildHeadlineKpi(
                        "monthly_active_users",
                        "Monthly active users",
                        monthlyActiveUsers.toDouble(),
                        "Last 30 days",
                        monthlyActiveUsers.toDouble(),
                        previousMonthlyActiveUsers.toDouble()
                    ),
                    buildHeadlineKpi(
                        "success_rate",
                        "Success rate",
                        successRate(last30DaysMetrics),
                        "Last 30 days",
                        successRate(last30DaysMetrics),
                        successRate(previous30DaysMetrics)
                    ),
                    buildHeadlineKpi(
                        "avg_results_per_search",
                        "Avg results/search",
                        averageResults(last30DaysMetrics),
                        "Last 30 days",
                        averageResults(last30DaysMetrics),
                        averageResults(previous30DaysMetrics)
                    ),
                    buildHeadlineKpi(
                        "details_opened_30d",
                        "Details opened",
                        last30DaysMetrics.detailsOpened.toDouble(),
                        "Last 30 days",
                        last30DaysMetrics.detailsOpened.toDouble(),
                        previous30DaysMetrics.detailsOpened.toDouble()
                    ),
                    buildHeadlineKpi(
                        "route_clicks_30d",
                        "Route clicks",
                        last30DaysMetrics.routeClicks.toDouble(),
                        "Last 30 days",
                        last30DaysMetrics.routeClicks.toDouble(),
                        previous30DaysMetrics.routeClicks.toDouble()
                    )
                ),
            summary =
                PublicAnalyticsSummary(
                    totalUsers = totalUsers,
                    newUsersToday = newUsersToday,
                    newUsers7d = newUsers7d,
                    newUsers30d = newUsers30d,
                    dailyActiveUsers = dailyActiveUsers,
                    monthlyActiveUsers = monthlyActiveUsers,
                    totalSearches = lifetimeMetrics.totalSearches,
                    searchesToday = todayMetrics.totalSearches,
                    searches7d = last7DaysMetrics.totalSearches,
                    searches30d = last30DaysMetrics.totalSearches,
                    successfulSearches = lifetimeMetrics.successfulSearches,
                    emptySearches = lifetimeMetrics.emptySearches,
                    successRate = successRate(lifetimeMetrics),
                    averageResultsPerSearch = averageResults(lifetimeMetrics),
                    routeClicks = lifetimeMetrics.routeClicks,
                    detailsOpened = lifetimeMetrics.detailsOpened
                ),
            dailySeries = dailySeries,
            monthlySeries = monthlySeries,
            eventTotals = eventTotals
        )
    }

    private fun buildHeadlineKpi(
        key: String,
        label: String,
        value: Double,
        period: String,
        current: Double,
        previous: Double
    ): PublicAnalyticsHeadlineKpi {
        val changePct = percentageChange(current, previous)
        return PublicAnalyticsHeadlineKpi(
            key = key,
            label = label,
            value = round(value, 2),
            period = period,
            changePct = changePct,
            trend = trendOf(changePct)
        )
    }

    private fun successRate(metrics: PublicAnalyticsEventMetrics): Double {
        if (metrics.totalSearches == 0L) {
            return 0.0
        }
        return round(metrics.successfulSearches.toDouble() / metrics.totalSearches.toDouble() * 100.0, 2)
    }

    private fun averageResults(metrics: PublicAnalyticsEventMetrics): Double {
        if (metrics.totalSearches == 0L) {
            return 0.0
        }
        return round(metrics.totalResultsAcrossSearches.toDouble() / metrics.totalSearches.toDouble(), 2)
    }

    private fun percentageChange(
        current: Double,
        previous: Double
    ): Double {
        if (previous == 0.0) {
            return if (current == 0.0) 0.0 else 100.0
        }
        return round(((current - previous) / previous) * 100.0, 1)
    }

    private fun trendOf(changePct: Double): PublicAnalyticsTrend =
        when {
            changePct > 0.0 -> PublicAnalyticsTrend.UP
            changePct < 0.0 -> PublicAnalyticsTrend.DOWN
            else -> PublicAnalyticsTrend.FLAT
        }

    private fun round(
        value: Double,
        scale: Int
    ): Double =
        BigDecimal
            .valueOf(value)
            .setScale(scale, RoundingMode.HALF_UP)
            .toDouble()

    private fun rollingWindow(
        anchorDate: LocalDate,
        days: Long
    ): AnalyticsWindow = dateWindow(anchorDate.minusDays(days - 1), anchorDate.plusDays(1))

    private fun previousRollingWindow(
        anchorDate: LocalDate,
        days: Long
    ): AnalyticsWindow = dateWindow(anchorDate.minusDays((days * 2) - 1), anchorDate.minusDays(days - 1))

    private fun dayWindow(date: LocalDate): AnalyticsWindow = dateWindow(date, date.plusDays(1))

    private fun fillDailySeries(
        startDate: LocalDate,
        points: List<yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate>
    ): List<yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate> {
        val pointByDate = points.associateBy { LocalDate.parse(it.date) }
        return (0 until 30).map { offset ->
            val date = startDate.plusDays(offset.toLong())
            pointByDate[date]
                ?: yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate(
                    date = date.toString(),
                    activeUsers = 0,
                    searches = 0,
                    successfulSearches = 0,
                    emptySearches = 0,
                    routeClicks = 0,
                    detailsOpened = 0
                )
        }
    }

    private fun dateWindow(
        startDate: LocalDate,
        endDateExclusive: LocalDate
    ): AnalyticsWindow =
        AnalyticsWindow(
            start = startDate.atStartOfDay(zoneId).toInstant(),
            end = endDateExclusive.atStartOfDay(zoneId).toInstant()
        )

    private fun eventLabel(eventKey: String): String? =
        when (eventKey) {
            ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value -> "Searches with results"
            ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value -> "No results searches"
            ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value -> "Details opened"
            ProductAnalyticsEvent.ROUTE_CLICKED.value -> "Route opened"
            else -> null
        }
}
