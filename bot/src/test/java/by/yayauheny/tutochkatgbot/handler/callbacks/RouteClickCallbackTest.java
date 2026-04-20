package by.yayauheny.tutochkatgbot.handler.callbacks;

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
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.service.SearchService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RouteClickCallbackTest {
    @Mock
    private MessageSender sender;
    @Mock
    private SearchService searchService;

    private SimpleMeterRegistry registry;
    private BotMetrics botMetrics;
    private RouteClickCallback handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new SimpleMeterRegistry();
        botMetrics = new BotMetrics(registry);
        handler = new RouteClickCallback(sender, searchService, botMetrics);
    }

    @Test
    void routeClickShouldFailFastForUnknownProviderAndStillCountClick() {
        Update update = createRouteUpdate("route:unknown:missing-id");
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        verify(sender).safeReply(ctx, "Не удалось открыть маршрут, попробуйте снова.");
        verify(searchService, never()).getById(anyString());
        verify(sender, never()).sendText(anyLong(), anyString());

        double count =
            registry.get("route_click_total")
                .tag("client_type", "telegram_bot")
                .tag("provider", "unknown")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void routeClickShouldStillUseKnownProviderForLinkBuilding() {
        UUID restroomId = UUID.randomUUID();
        when(searchService.getById(restroomId.toString())).thenReturn(Optional.of(sampleRestroom(restroomId)));

        Update update = createRouteUpdate(CallbackData.route("google", restroomId.toString()));
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        double count =
            registry.get("route_click_total")
                .tag("client_type", "telegram_bot")
                .tag("provider", "google")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    private Update createRouteUpdate(String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(Chat.builder().id(123L).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
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
}
