package by.yayauheny.tutochkatgbot.metrics;

import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BotMetricsTest {

    @Test
    void incrementTelegramUpdate_shouldIncreaseCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BotMetrics metrics = new BotMetrics(registry);

        metrics.incrementTelegramUpdate("location");

        double count = registry.get("telegram_updates_total")
            .tag("type", "location")
            .counter()
            .count();

        assertEquals(1.0, count);
    }

    @Test
    void incrementBackendRequest_shouldIncreaseCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BotMetrics metrics = new BotMetrics(registry);

        metrics.incrementBackendRequest("/restrooms/nearest", "success");

        double count = registry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "success")
            .counter()
            .count();

        assertEquals(1.0, count);
    }

    @Test
    void stopBackendTimer_shouldRecordTimer() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BotMetrics metrics = new BotMetrics(registry);

        Timer.Sample sample = metrics.startBackendTimer();
        metrics.stopBackendTimer(sample, "/restrooms/nearest");

        long count = registry.get("bot_backend_request_duration_seconds")
            .tag("endpoint", "/restrooms/nearest")
            .timer()
            .count();

        assertEquals(1L, count);
    }

    @Test
    void incrementRouteClick_shouldIncreaseCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BotMetrics metrics = new BotMetrics(registry);

        metrics.incrementRouteClick("telegram_bot", "google");

        double count = registry.get("route_click_total")
            .tag("client_type", "telegram_bot")
            .tag("provider", "google")
            .counter()
            .count();

        assertEquals(1.0, count);
    }

    @Test
    void incrementBackListCache_shouldIncreaseCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        BotMetrics metrics = new BotMetrics(registry);

        metrics.incrementBackListCache("hit");

        double count =
            registry.get("bot_back_list_cache_total")
                .tag("outcome", "hit")
                .counter()
                .count();

        assertEquals(1.0, count);
    }
}
