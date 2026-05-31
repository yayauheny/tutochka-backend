package yayauheny.by.analytics.repository

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import java.sql.ResultSet
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

@Repository
class PublicAnalyticsRepositoryImpl(
    private val jdbcTemplate: NamedParameterJdbcTemplate
) : PublicAnalyticsRepository {

    override fun countTotalUsers(): Long {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", MapSqlParameterSource(), Long::class.java) ?: 0L
    }

    override fun countNewUsers(window: AnalyticsWindow): Long {
        val params = MapSqlParameterSource()
            .addValue("start", java.sql.Timestamp.from(window.start))
            .addValue("end", java.sql.Timestamp.from(window.end))
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM users WHERE created_at >= :start AND created_at < :end",
            params,
            Long::class.java
        ) ?: 0L
    }

    override fun countActiveUsers(window: AnalyticsWindow): Long {
        val params = MapSqlParameterSource()
            .addValue("start", java.sql.Timestamp.from(window.start))
            .addValue("end", java.sql.Timestamp.from(window.end))
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(DISTINCT user_id) FROM analytics_events WHERE user_id IS NOT NULL AND created_at >= :start AND created_at < :end",
            params,
            Long::class.java
        ) ?: 0L
    }

    override fun aggregateEventMetrics(window: AnalyticsWindow?): PublicAnalyticsEventMetrics {
        val params = MapSqlParameterSource()
        val condition = if (window != null) {
            params.addValue("start", java.sql.Timestamp.from(window.start))
            params.addValue("end", java.sql.Timestamp.from(window.end))
            "WHERE created_at >= :start AND created_at < :end"
        } else {
            ""
        }

        val sql = """
            SELECT
                SUM(CASE WHEN event IN ('nearest_restrooms_returned', 'nearest_restrooms_no_results') THEN 1 ELSE 0 END) as total_searches,
                SUM(CASE WHEN event = 'nearest_restrooms_returned' THEN 1 ELSE 0 END) as successful_searches,
                SUM(CASE WHEN event = 'nearest_restrooms_no_results' THEN 1 ELSE 0 END) as empty_searches,
                SUM(CASE WHEN event IN ('nearest_restrooms_returned', 'nearest_restrooms_no_results') THEN COALESCE(results_count, 0) ELSE 0 END) as total_results,
                SUM(CASE WHEN event = 'route_clicked' THEN 1 ELSE 0 END) as route_clicks,
                SUM(CASE WHEN event = 'restroom_details_opened' THEN 1 ELSE 0 END) as details_opened
            FROM analytics_events
            $condition
        """.trimIndent()

        return jdbcTemplate.queryForObject(sql, params) { rs, _ ->
            PublicAnalyticsEventMetrics(
                totalSearches = rs.getLong("total_searches"),
                successfulSearches = rs.getLong("successful_searches"),
                emptySearches = rs.getLong("empty_searches"),
                totalResultsAcrossSearches = rs.getLong("total_results"),
                routeClicks = rs.getLong("route_clicks"),
                detailsOpened = rs.getLong("details_opened")
            )
        } ?: PublicAnalyticsEventMetrics(0, 0, 0, 0, 0, 0)
    }

    override fun fetchDailySeries(
        window: AnalyticsWindow,
        zoneId: ZoneId
    ): List<PublicAnalyticsDailyAggregate> {
        val sql = """
            SELECT
                date(timezone(:zoneId, created_at)) as day,
                COUNT(DISTINCT user_id) as active_users,
                SUM(CASE WHEN event IN ('nearest_restrooms_returned', 'nearest_restrooms_no_results') THEN 1 ELSE 0 END) as searches,
                SUM(CASE WHEN event = 'nearest_restrooms_returned' THEN 1 ELSE 0 END) as successful_searches,
                SUM(CASE WHEN event = 'nearest_restrooms_no_results' THEN 1 ELSE 0 END) as empty_searches,
                SUM(CASE WHEN event = 'route_clicked' THEN 1 ELSE 0 END) as route_clicks,
                SUM(CASE WHEN event = 'restroom_details_opened' THEN 1 ELSE 0 END) as details_opened
            FROM analytics_events
            WHERE created_at >= :start AND created_at < :end
            GROUP BY 1
            ORDER BY 1 ASC
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("zoneId", zoneId.id)
            .addValue("start", java.sql.Timestamp.from(window.start))
            .addValue("end", java.sql.Timestamp.from(window.end))

        return jdbcTemplate.query(sql, params) { rs, _ ->
            PublicAnalyticsDailyAggregate(
                date = rs.getString("day"),
                activeUsers = rs.getLong("active_users"),
                searches = rs.getLong("searches"),
                successfulSearches = rs.getLong("successful_searches"),
                emptySearches = rs.getLong("empty_searches"),
                routeClicks = rs.getLong("route_clicks"),
                detailsOpened = rs.getLong("details_opened")
            )
        }
    }

    override fun fetchMonthlySeries(
        startMonth: YearMonth,
        monthsCount: Int,
        zoneId: ZoneId
    ): List<PublicAnalyticsMonthlyAggregate> {
        val start = startMonth.atDay(1).atStartOfDay(zoneId).toInstant()
        val end = startMonth.plusMonths(monthsCount.toLong()).atDay(1).atStartOfDay(zoneId).toInstant()

        val eventRows = fetchMonthlyEventRows(start, end, zoneId)
        val newUsersRows = fetchMonthlyNewUsers(start, end, zoneId)

        val months = (0 until monthsCount).associate { offset ->
            val month = startMonth.plusMonths(offset.toLong()).toString()
            month to PublicAnalyticsMonthlyAggregate(month = month, newUsers = 0, activeUsers = 0, searches = 0)
        }.toMutableMap()

        newUsersRows.forEach { row ->
            months[row.month] = months.getValue(row.month).copy(newUsers = row.newUsers)
        }
        eventRows.forEach { row ->
            months[row.month] = months.getValue(row.month).copy(
                activeUsers = row.activeUsers,
                searches = row.searches
            )
        }

        return months.values.toList()
    }

    override fun fetchEventTotals(minimumCount: Long): List<PublicAnalyticsEventCount> {
        val sql = """
            SELECT event, COUNT(*) as event_count
            FROM analytics_events
            GROUP BY event
            HAVING COUNT(*) >= :minCount
            ORDER BY event_count DESC
        """.trimIndent()

        return jdbcTemplate.query(sql, MapSqlParameterSource("minCount", minimumCount)) { rs, _ ->
            PublicAnalyticsEventCount(
                eventKey = rs.getString("event"),
                count = rs.getLong("event_count")
            )
        }
    }

    private fun fetchMonthlyEventRows(start: Instant, end: Instant, zoneId: ZoneId): List<MonthlyEventRow> {
        val sql = """
            SELECT
                to_char(date_trunc('month', timezone(:zoneId, created_at)), 'YYYY-MM') as month,
                COUNT(DISTINCT user_id) as active_users,
                SUM(CASE WHEN event IN ('nearest_restrooms_returned', 'nearest_restrooms_no_results') THEN 1 ELSE 0 END) as searches
            FROM analytics_events
            WHERE created_at >= :start AND created_at < :end
            GROUP BY month
            ORDER BY month ASC
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("zoneId", zoneId.id)
            .addValue("start", java.sql.Timestamp.from(start))
            .addValue("end", java.sql.Timestamp.from(end))

        return jdbcTemplate.query(sql, params) { rs, _ ->
            MonthlyEventRow(
                month = rs.getString("month"),
                activeUsers = rs.getLong("active_users"),
                searches = rs.getLong("searches")
            )
        }
    }

    private fun fetchMonthlyNewUsers(start: Instant, end: Instant, zoneId: ZoneId): List<MonthlyNewUsersRow> {
        val sql = """
            SELECT
                to_char(date_trunc('month', timezone(:zoneId, created_at)), 'YYYY-MM') as month,
                COUNT(*) as new_users
            FROM users
            WHERE created_at >= :start AND created_at < :end
            GROUP BY month
            ORDER BY month ASC
        """.trimIndent()

        val params = MapSqlParameterSource()
            .addValue("zoneId", zoneId.id)
            .addValue("start", java.sql.Timestamp.from(start))
            .addValue("end", java.sql.Timestamp.from(end))

        return jdbcTemplate.query(sql, params) { rs, _ ->
            MonthlyNewUsersRow(
                month = rs.getString("month"),
                newUsers = rs.getLong("new_users")
            )
        }
    }

    private data class MonthlyNewUsersRow(val month: String, val newUsers: Long)
    private data class MonthlyEventRow(val month: String, val activeUsers: Long, val searches: Long)
}
