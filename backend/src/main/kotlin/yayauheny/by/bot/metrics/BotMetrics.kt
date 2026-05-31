package yayauheny.by.bot.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component

@Component
class BotMetrics(private val registry: MeterRegistry) {

    companion object {
        const val ENDPOINT_NEAREST = "/restrooms/nearest"
        const val ENDPOINT_BY_ID = "/restrooms/{id}"
    }

    fun incrementTelegramUpdate(type: String?) {
        Counter.builder("telegram_updates_total")
            .description("Incoming Telegram updates")
            .tag("type", BotMetricLabelWhitelist.normalizeUpdateType(type))
            .register(registry)
            .increment()
    }

    fun incrementBackendRequest(endpoint: String?, outcome: String?) {
        Counter.builder("bot_backend_requests_total")
            .description("Bot to backend requests")
            .tag("endpoint", BotMetricLabelWhitelist.normalizeEndpoint(endpoint))
            .tag("outcome", BotMetricLabelWhitelist.normalizeOutcome(outcome))
            .register(registry)
            .increment()
    }

    fun startBackendTimer(): Timer.Sample {
        return Timer.start(registry)
    }

    fun stopBackendTimer(sample: Timer.Sample, endpoint: String?) {
        sample.stop(
            Timer.builder("bot_backend_request_duration_seconds")
                .description("Bot to backend request duration")
                .tag("endpoint", BotMetricLabelWhitelist.normalizeEndpoint(endpoint))
                .register(registry)
        )
    }

    fun incrementBackListCache(outcome: String?) {
        Counter.builder("bot_back_list_cache_total")
            .description("Back-to-list cache outcomes")
            .tag("outcome", BotMetricLabelWhitelist.normalizeCacheOutcome(outcome))
            .register(registry)
            .increment()
    }
}
