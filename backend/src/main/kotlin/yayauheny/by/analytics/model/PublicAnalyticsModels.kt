package yayauheny.by.analytics.model

import java.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Contextual

data class AnalyticsWindow(
    val start: Instant,
    val end: Instant
)

data class PublicAnalyticsEventMetrics(
    val totalSearches: Long = 0,
    val successfulSearches: Long = 0,
    val emptySearches: Long = 0,
    val totalResultsAcrossSearches: Long = 0,
    val routeClicks: Long = 0,
    val detailsOpened: Long = 0
)

data class PublicAnalyticsDailyAggregate(
    val date: String,
    val activeUsers: Long,
    val searches: Long,
    val successfulSearches: Long,
    val emptySearches: Long,
    val routeClicks: Long,
    val detailsOpened: Long
)

data class PublicAnalyticsMonthlyAggregate(
    val month: String,
    val newUsers: Long,
    val activeUsers: Long,
    val searches: Long
)

data class PublicAnalyticsEventCount(
    val eventKey: String,
    val count: Long
)

@Serializable
enum class PublicAnalyticsTrend {
    @SerialName("up")
    UP,

    @SerialName("down")
    DOWN,

    @SerialName("flat")
    FLAT
}

@Serializable
data class PublicAnalyticsHeadlineKpi(
    val key: String,
    val label: String,
    val value: Double,
    val period: String,
    val changePct: Double,
    val trend: PublicAnalyticsTrend
)

@Serializable
data class PublicAnalyticsSummary(
    val totalUsers: Long,
    val newUsersToday: Long,
    val newUsers7d: Long,
    val newUsers30d: Long,
    val dailyActiveUsers: Long,
    val monthlyActiveUsers: Long,
    val totalSearches: Long,
    val searchesToday: Long,
    val searches7d: Long,
    val searches30d: Long,
    val successfulSearches: Long,
    val emptySearches: Long,
    val successRate: Double,
    val averageResultsPerSearch: Double,
    val routeClicks: Long,
    val detailsOpened: Long
)

@Serializable
data class PublicAnalyticsDailySeriesPoint(
    val date: String,
    val activeUsers: Long,
    val searches: Long,
    val successfulSearches: Long,
    val emptySearches: Long,
    val routeClicks: Long,
    val detailsOpened: Long
)

@Serializable
data class PublicAnalyticsMonthlySeriesPoint(
    val month: String,
    val newUsers: Long,
    val activeUsers: Long,
    val searches: Long
)

@Serializable
data class PublicAnalyticsEventTotal(
    val eventKey: String,
    val label: String,
    val count: Long
)

@Serializable
data class PublicAnalyticsResponse(
    @Contextual val generatedAt: Instant,
    val timezone: String,
    val headlineKpis: List<PublicAnalyticsHeadlineKpi>,
    val summary: PublicAnalyticsSummary,
    val dailySeries: List<PublicAnalyticsDailySeriesPoint>,
    val monthlySeries: List<PublicAnalyticsMonthlySeriesPoint>,
    val eventTotals: List<PublicAnalyticsEventTotal>
)
