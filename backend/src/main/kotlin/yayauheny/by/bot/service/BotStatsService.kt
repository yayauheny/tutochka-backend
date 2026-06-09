package yayauheny.by.bot.service

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.springframework.stereotype.Service
import yayauheny.by.analytics.model.AnalyticsWindow
import yayauheny.by.analytics.repository.PublicAnalyticsRepository

@Service
class BotStatsService(
    private val publicAnalyticsRepository: PublicAnalyticsRepository,
    private val userService: UserService,
    private val clock: Clock
) {
    private val zoneId = ZoneId.of("Europe/Minsk")

    fun loadToday(userId: Long): String = loadAndRemember(userId, BotStatsPeriod.TODAY)

    fun loadLast7Days(userId: Long): String = loadAndRemember(userId, BotStatsPeriod.LAST_7_DAYS)

    fun loadCurrentMonth(userId: Long): String = loadAndRemember(userId, BotStatsPeriod.CURRENT_MONTH)

    fun reloadLastOrToday(userId: Long): String {
        val period = BotStatsPeriod.fromKey(userService.getLastStatsPeriod(userId)) ?: BotStatsPeriod.TODAY
        return loadAndRemember(userId, period)
    }

    private fun loadAndRemember(
        userId: Long,
        period: BotStatsPeriod
    ): String {
        userService.saveLastStatsPeriod(userId, period.key)
        return formatStats(loadSnapshot(period))
    }

    private fun loadSnapshot(period: BotStatsPeriod): BotStatsSnapshot {
        val today = LocalDate.now(clock.withZone(zoneId))
        val window = period.window(today, zoneId)
        val metrics = publicAnalyticsRepository.aggregateEventMetrics(window)

        return BotStatsSnapshot(
            title = period.title,
            newUsers = publicAnalyticsRepository.countNewUsers(window),
            returningActiveUsers = publicAnalyticsRepository.countReturningActiveUsers(window),
            totalActiveUsers = publicAnalyticsRepository.countActiveUsers(window),
            totalUsers = publicAnalyticsRepository.countTotalUsers(),
            searches = metrics.totalSearches,
            successfulSearches = metrics.successfulSearches,
            emptySearches = metrics.emptySearches,
            detailsOpened = metrics.detailsOpened,
            successRate = successRate(metrics.successfulSearches, metrics.totalSearches)
        )
    }

    private fun formatStats(stats: BotStatsSnapshot): String =
        """
        📊 <b>Статистика за ${stats.title}</b>
        
        👤 <b>Пользователи</b>
        Новые: ${stats.newUsers}
        Вернувшиеся активные: ${stats.returningActiveUsers}
        Всего использовали: ${stats.totalActiveUsers}
        Всего пользователей: ${stats.totalUsers}
        
        🔎 <b>Поиск</b>
        Поисков: ${stats.searches}
        Успешных: ${stats.successfulSearches}
        Без результатов: ${stats.emptySearches}
        Открытий карточек: ${stats.detailsOpened}
        Успешность: ${formatPercentage(stats.successRate)}
        """.trimIndent()

    private fun successRate(
        successfulSearches: Long,
        totalSearches: Long
    ): Double {
        if (totalSearches == 0L) {
            return 0.0
        }
        return round(successfulSearches.toDouble() / totalSearches.toDouble() * 100.0)
    }

    private fun formatPercentage(value: Double): String = "${round(value)}%"

    private fun round(value: Double): Double =
        BigDecimal.valueOf(value)
            .setScale(1, RoundingMode.HALF_UP)
            .toDouble()
}

enum class BotStatsPeriod(
    val key: String,
    val title: String
) {
    TODAY("today", "сегодня") {
        override fun window(
            today: LocalDate,
            zoneId: ZoneId
        ): AnalyticsWindow = dateWindow(today, today.plusDays(1), zoneId)
    },
    LAST_7_DAYS("last_7_days", "последние 7 дней") {
        override fun window(
            today: LocalDate,
            zoneId: ZoneId
        ): AnalyticsWindow = dateWindow(today.minusDays(6), today.plusDays(1), zoneId)
    },
    CURRENT_MONTH("current_month", "текущий месяц") {
        override fun window(
            today: LocalDate,
            zoneId: ZoneId
        ): AnalyticsWindow {
            val startDate = YearMonth.from(today).atDay(1)
            return dateWindow(startDate, today.plusDays(1), zoneId)
        }
    };

    abstract fun window(
        today: LocalDate,
        zoneId: ZoneId
    ): AnalyticsWindow

    companion object {
        fun fromKey(key: String?): BotStatsPeriod? = entries.firstOrNull { it.key == key }

        private fun dateWindow(
            startDate: LocalDate,
            endDateExclusive: LocalDate,
            zoneId: ZoneId
        ): AnalyticsWindow =
            AnalyticsWindow(
                start = startDate.atStartOfDay(zoneId).toInstant(),
                end = endDateExclusive.atStartOfDay(zoneId).toInstant()
            )
    }
}

private data class BotStatsSnapshot(
    val title: String,
    val newUsers: Long,
    val returningActiveUsers: Long,
    val totalActiveUsers: Long,
    val totalUsers: Long,
    val searches: Long,
    val successfulSearches: Long,
    val emptySearches: Long,
    val detailsOpened: Long,
    val successRate: Double
)
