package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.dto.backend.AccessibilityType;
import by.yayauheny.tutochkatgbot.dto.backend.DataSourceType;
import by.yayauheny.tutochkatgbot.dto.backend.ImportProvider;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.LocationType;
import by.yayauheny.tutochkatgbot.dto.backend.PlaceType;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomStatus;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import by.yayauheny.tutochkatgbot.service.UpdateHandlingService;
import io.micrometer.core.instrument.MeterRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TutochkaTgBotApplication.class)
@TestPropertySource(properties = {
    "telegram.bot.username=test_bot",
    "telegram.bot.token=test_token",
    "backend.base-url=http://localhost:9999",
    "bot.async-processing=false",
    "bot.webhook-public-url="
})
class BotMetricsIntegrationTest {

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
    void backend4xxShouldIncrementOutcomeCounter() {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        updateRouter.route(locationUpdate(53.9, 27.56));

        double count = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "4xx")
            .counter()
            .count();

        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void updateHandlingServiceShouldIncrementTelegramUpdateMetric() {
        updateHandlingService.handle(locationUpdate(53.9, 27.56));

        double updates = meterRegistry.get("telegram_updates_total")
            .tag("type", "location")
            .counter()
            .count();

        assertThat(updates).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void backend5xxShouldIncrementOutcomeCounter() {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        updateRouter.route(locationUpdate(53.9, 27.56));

        double count = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "5xx")
            .counter()
            .count();

        assertThat(count).isGreaterThanOrEqualTo(1.0);
    }

    @Test
    void detailCallbackShouldRecordBotBackendMetricsForByIdEndpoint() {
        UUID restroomId = UUID.randomUUID();
        when(backendClient.getById(restroomId.toString()))
            .thenReturn(Optional.of(sampleRestroom(restroomId)));

        updateRouter.route(routeUpdate(123L, 10L, CallbackData.detail(restroomId.toString())));

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
    void customMetricsShouldNotExposeForbiddenLabelsOrValues() {
        when(backendClient.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(List.of());

        updateRouter.route(locationUpdate(53.9, 27.56));

        assertNoForbiddenMetricTags();
    }

    private Update locationUpdate(double lat, double lon) {
        Update update = new Update();
        Message message = new Message();
        Chat chat = Chat.builder().id(123L).type("private").build();
        message.setChat(chat);
        message.setMessageId(1);
        User from = User.builder().id(10L).isBot(false).firstName("Test").build();
        message.setFrom(from);
        Location location = Location.builder().latitude(lat).longitude(lon).build();
        message.setLocation(location);
        update.setMessage(message);
        return update;
    }

    private Update routeUpdate(long chatId, long userId, String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(2);
        message.setChat(Chat.builder().id(chatId).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(userId).isBot(false).firstName("Test").build());
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private RestroomResponseDto sampleRestroom(UUID restroomId) {
        return new RestroomResponseDto(
            restroomId,
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            null,
            null,
            null,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new LatLon(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            null,
            Map.of(),
            null,
            null,
            false,
            false,
            LocationType.UNKNOWN,
            ImportProvider.USER,
            null,
            false,
            Instant.now(),
            Instant.now(),
            100,
            null,
            null
        );
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
