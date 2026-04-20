package by.yayauheny.tutochkatgbot.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class BotMetrics {

    public static final String ENDPOINT_NEAREST = "/restrooms/nearest";
    public static final String ENDPOINT_BY_ID = "/restrooms/{id}";

    private final MeterRegistry registry;

    public BotMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void incrementTelegramUpdate(String type) {
        Counter.builder("telegram_updates_total")
            .description("Incoming Telegram updates")
            .tag("type", BotMetricLabelWhitelist.normalizeUpdateType(type))
            .register(registry)
            .increment();
    }

    public void incrementBackendRequest(String endpoint, String outcome) {
        Counter.builder("bot_backend_requests_total")
            .description("Bot to backend requests")
            .tag("endpoint", BotMetricLabelWhitelist.normalizeEndpoint(endpoint))
            .tag("outcome", BotMetricLabelWhitelist.normalizeOutcome(outcome))
            .register(registry)
            .increment();
    }

    public Timer.Sample startBackendTimer() {
        return Timer.start(registry);
    }

    public void stopBackendTimer(Timer.Sample sample, String endpoint) {
        sample.stop(
            Timer.builder("bot_backend_request_duration_seconds")
                .description("Bot to backend request duration")
                .tag("endpoint", BotMetricLabelWhitelist.normalizeEndpoint(endpoint))
                .register(registry)
        );
    }

    public void incrementRouteClick(String clientType, String provider) {
        Counter.builder("route_click_total")
            .description("Route link clicks")
            .tag("client_type", BotMetricLabelWhitelist.normalizeClientType(clientType))
            .tag("provider", BotMetricLabelWhitelist.normalizeProvider(provider))
            .register(registry)
            .increment();
    }

    public void incrementBackListCache(String outcome) {
        Counter.builder("bot_back_list_cache_total")
            .description("Back-to-list cache outcomes")
            .tag("outcome", BotMetricLabelWhitelist.normalizeCacheOutcome(outcome))
            .register(registry)
            .increment();
    }
}
