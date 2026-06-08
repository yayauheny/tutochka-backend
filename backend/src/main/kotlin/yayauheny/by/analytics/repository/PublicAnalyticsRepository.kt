package yayauheny.by.analytics.repository

import java.time.YearMonth
import java.time.ZoneId
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate

interface PublicAnalyticsRepository {
    fun countTotalUsers(): Long

    fun countNewUsers(window: AnalyticsWindow): Long

    fun countActiveUsers(window: AnalyticsWindow): Long

    fun aggregateEventMetrics(window: AnalyticsWindow? = null): PublicAnalyticsEventMetrics

    fun fetchDailySeries(
        window: AnalyticsWindow,
        zoneId: ZoneId
    ): List<PublicAnalyticsDailyAggregate>

    fun fetchMonthlySeries(
        startMonth: YearMonth,
        monthsCount: Int,
        zoneId: ZoneId
    ): List<PublicAnalyticsMonthlyAggregate>

    fun fetchEventTotals(): List<PublicAnalyticsEventCount>
}
