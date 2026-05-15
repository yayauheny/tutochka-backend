package yayauheny.by.analytics.repository

import java.time.YearMonth
import java.time.ZoneId
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate

interface PublicAnalyticsRepository {
    suspend fun countTotalUsers(): Long

    suspend fun countNewUsers(window: AnalyticsWindow): Long

    suspend fun countActiveUsers(window: AnalyticsWindow): Long

    suspend fun aggregateEventMetrics(window: AnalyticsWindow? = null): PublicAnalyticsEventMetrics

    suspend fun fetchDailySeries(
        window: AnalyticsWindow,
        zoneId: ZoneId
    ): List<PublicAnalyticsDailyAggregate>

    suspend fun fetchMonthlySeries(
        startMonth: YearMonth,
        monthsCount: Int,
        zoneId: ZoneId
    ): List<PublicAnalyticsMonthlyAggregate>

    suspend fun fetchEventTotals(minimumCount: Long): List<PublicAnalyticsEventCount>
}
