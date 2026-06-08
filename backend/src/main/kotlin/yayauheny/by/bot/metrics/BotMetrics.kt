package yayauheny.by.bot.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component

@Component
class BotMetrics(private val registry: MeterRegistry) {
    fun incrementTelegramUpdate(type: String?) {
        Counter.builder("telegram_updates_total")
            .description("Incoming Telegram updates")
            .tag("type", BotMetricLabelWhitelist.normalizeUpdateType(type))
            .register(registry)
            .increment()
    }

    fun incrementBackListCache(outcome: String?) {
        Counter.builder("bot_back_list_cache_total")
            .description("Back-to-list cache outcomes")
            .tag("outcome", BotMetricLabelWhitelist.normalizeCacheOutcome(outcome))
            .register(registry)
            .increment()
    }
}
