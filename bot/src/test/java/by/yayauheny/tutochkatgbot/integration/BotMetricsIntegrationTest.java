package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import by.yayauheny.tutochkatgbot.service.UpdateHandlingService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

class BotMetricsIntegrationTest extends AbstractSpringBotIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @Autowired
    private UpdateHandlingService updateHandlingService;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void backend4xxShouldIncrementOutcomeCounter() throws Exception {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        assertThrows(HttpClientErrorException.class, () -> updateRouter.route(locationUpdate(123L, 10L, 53.9, 27.56)));

        double count = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "4xx")
            .counter()
            .count();

        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void updateHandlingServiceShouldIncrementTelegramUpdateMetric() throws Exception {
        updateHandlingService.handle(locationUpdate(123L, 10L, 53.9, 27.56));

        double updates = meterRegistry.get("telegram_updates_total")
            .tag("type", "location")
            .counter()
            .count();

        assertThat(updates).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void backend5xxShouldIncrementOutcomeCounter() throws Exception {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpServerErrorException.class, () -> updateRouter.route(locationUpdate(123L, 10L, 53.9, 27.56)));

        double count = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "5xx")
            .counter()
            .count();

        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void detailCallbackShouldRecordBotBackendMetricsForByIdEndpoint() throws Exception {
        UUID restroomId = UUID.randomUUID();
        when(backendClient.getById(restroomId.toString()))
            .thenReturn(Optional.of(restroomResponse(restroomId, twoGisExternalMap("abc123"))));

        updateRouter.route(callbackUpdate(123L, 10L, CallbackData.detail(restroomId.toString())));

        double count = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/{id}")
            .tag("outcome", "success")
            .counter()
            .count();

        long timerCount = meterRegistry.get("bot_backend_request_duration_seconds")
            .tag("endpoint", "/restrooms/{id}")
            .timer()
            .count();

        assertThat(count).isGreaterThanOrEqualTo(1.0);
        assertThat(timerCount).isGreaterThanOrEqualTo(1L);
        assertNoForbiddenMetricTags();
    }

    @Test
    void customMetricsShouldNotExposeForbiddenLabelsOrValues() throws Exception {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(List.of());

        updateRouter.route(locationUpdate(123L, 10L, 53.9, 27.56));

        assertNoForbiddenMetricTags();
    }

    private void assertNoForbiddenMetricTags() {
        for (var meter : meterRegistry.getMeters()) {
            for (var tag : meter.getId().getTags()) {
                String key = tag.getKey();
                String value = tag.getValue();
                assertThat(key)
                    .as("Forbidden metric tag key: %s=%s on meter %s", key, value, meter.getId().getName())
                    .isNotIn("lat", "lon", "chat_id", "user_id", "restroom_id");
                assertThat(value)
                    .as("Forbidden metric tag value: %s=%s on meter %s", key, value, meter.getId().getName())
                    .isNotIn("lat", "lon", "chat_id", "user_id", "restroom_id");
            }
        }
    }
}
