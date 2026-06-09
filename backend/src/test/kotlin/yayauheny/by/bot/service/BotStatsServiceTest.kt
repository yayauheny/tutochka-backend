package yayauheny.by.bot.service

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.assertEquals
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.model.PublicAnalyticsDailyAggregate
import yayauheny.by.analytics.model.PublicAnalyticsEventCount
import yayauheny.by.analytics.model.PublicAnalyticsEventMetrics
import yayauheny.by.analytics.model.PublicAnalyticsMonthlyAggregate
import yayauheny.by.analytics.repository.PublicAnalyticsRepository
import yayauheny.by.bot.session.InMemorySessionStore

class BotStatsServiceTest {
    private val zoneId = ZoneId.of("Europe/Minsk")
    private val fixedClock = Clock.fixed(Instant.parse("2026-06-09T09:00:00Z"), ZoneOffset.UTC)
    private val today = fixedClock.instant().atZone(zoneId).toLocalDate()
    private val userService = UserService(InMemorySessionStore())

    @Test
    fun `loadToday returns formatted report and remembers selection`() {
        val todayWindow = dayWindow(today)
        val repository =
            FakePublicAnalyticsRepository(
                totalUsers = 512,
                newUsersByWindow = mapOf(todayWindow to 12),
                activeUsersByWindow = mapOf(todayWindow to 46),
                returningActiveUsersByWindow = mapOf(todayWindow to 34),
                metricsByWindow = mapOf(todayWindow to PublicAnalyticsEventMetrics(71, 63, 8, 0, 29))
            )

        val report = BotStatsService(repository, userService, fixedClock).loadToday(42L)

        assertThat(report).contains("Статистика за сегодня")
        assertThat(report).contains("Новые: 12")
        assertThat(report).contains("Вернувшиеся активные: 34")
        assertThat(report).contains("Всего использовали: 46")
        assertThat(report).contains("Всего пользователей: 512")
        assertThat(report).contains("Успешность: 88.7%")
        assertEquals("today", userService.getLastStatsPeriod(42L))
    }

    @Test
    fun `reloadLastOrToday falls back to today when nothing stored`() {
        val todayWindow = dayWindow(today)
        val repository =
            FakePublicAnalyticsRepository(
                totalUsers = 20,
                newUsersByWindow = mapOf(todayWindow to 2),
                activeUsersByWindow = mapOf(todayWindow to 5),
                returningActiveUsersByWindow = mapOf(todayWindow to 3),
                metricsByWindow = mapOf(todayWindow to PublicAnalyticsEventMetrics(4, 3, 1, 0, 2))
            )

        val report = BotStatsService(repository, userService, fixedClock).reloadLastOrToday(7L)

        assertThat(report).contains("Статистика за сегодня")
        assertEquals("today", userService.getLastStatsPeriod(7L))
    }

    private fun dayWindow(date: LocalDate): AnalyticsWindow =
        AnalyticsWindow(
            start = date.atStartOfDay(zoneId).toInstant(),
            end = date.plusDays(1).atStartOfDay(zoneId).toInstant()
        )

    private class FakePublicAnalyticsRepository(
        private val totalUsers: Long,
        private val newUsersByWindow: Map<AnalyticsWindow, Long>,
        private val activeUsersByWindow: Map<AnalyticsWindow, Long>,
        private val returningActiveUsersByWindow: Map<AnalyticsWindow, Long>,
        private val metricsByWindow: Map<AnalyticsWindow, PublicAnalyticsEventMetrics>
    ) : PublicAnalyticsRepository {
        override fun countTotalUsers(): Long = totalUsers

        override fun countNewUsers(window: AnalyticsWindow): Long = newUsersByWindow[window] ?: 0

        override fun countActiveUsers(window: AnalyticsWindow): Long = activeUsersByWindow[window] ?: 0

        override fun countReturningActiveUsers(window: AnalyticsWindow): Long = returningActiveUsersByWindow[window] ?: 0

        override fun aggregateEventMetrics(window: AnalyticsWindow?): PublicAnalyticsEventMetrics =
            metricsByWindow[window] ?: PublicAnalyticsEventMetrics()

        override fun fetchDailySeries(
            window: AnalyticsWindow,
            zoneId: ZoneId
        ): List<PublicAnalyticsDailyAggregate> = emptyList()

        override fun fetchMonthlySeries(
            startMonth: YearMonth,
            monthsCount: Int,
            zoneId: ZoneId
        ): List<PublicAnalyticsMonthlyAggregate> = emptyList()

        override fun fetchEventTotals(): List<PublicAnalyticsEventCount> = emptyList()
    }
}
