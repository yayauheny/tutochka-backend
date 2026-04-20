package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.handler.callbacks.BackToListCallback;
import by.yayauheny.tutochkatgbot.handler.messages.LocationMessageHandler;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackToListCacheIntegrationTest {
    @Mock
    private MessageSender sender;
    @Mock
    private SearchService searchService;
    @Mock
    private UserService userService;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private SimpleMeterRegistry registry;
    private FormatterService formatterService;
    private InlineKeyboardFactory inlineKeyboardFactory;
    private BackListSnapshotCache cache;
    private LocationMessageHandler locationHandler;
    private BackToListCallback backToListCallback;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new SimpleMeterRegistry();
        formatterService = new FormatterService();
        inlineKeyboardFactory = new InlineKeyboardFactory(formatterService);
        cache = BackListSnapshotCache.forTest(new BotMetrics(registry), com.github.benmanes.caffeine.cache.Ticker.systemTicker(), java.time.Clock.systemUTC(), java.time.Duration.ofMinutes(5), 5_000L);
        locationHandler =
            new LocationMessageHandler(
                sender,
                searchService,
                userService,
                cache,
                formatterService,
                inlineKeyboardFactory,
                replyKeyboard
            );
        backToListCallback =
            new BackToListCallback(
                sender,
                cache,
                formatterService,
                inlineKeyboardFactory,
                replyKeyboard
            );
    }

    @Test
    void locationThenBack_shouldRestoreCachedListWithoutBackendCall() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        double lat = 53.9;
        double lon = 27.56;

        Update locationUpdate = createLocationUpdate(chatId, userId, lat, lon);
        UpdateContext locationCtx = UpdateContext.from(locationUpdate);
        List<NearestRestroomSlimDto> results =
            List.of(
                sampleItem("Test restroom A", 250.0),
                sampleItem("Test restroom B", 320.0)
            );

        when(searchService.findNearby(lat, lon, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT))
            .thenReturn(results);

        locationHandler.handle(locationUpdate, locationCtx);

        Update backUpdate = createBackUpdate(chatId, userId);
        UpdateContext backCtx = UpdateContext.from(backUpdate);
        backToListCallback.handle(backUpdate, backCtx);

        verify(searchService).findNearby(lat, lon, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);
        verify(sender).editOrReply(eq(backCtx), eq(formatterService.toiletsFound(2)), any(InlineKeyboardMarkup.class));
        assertEquals(1.0, registry.get("bot_back_list_cache_total").tag("outcome", "hit").counter().count());
    }

    @Test
    void emptyResults_shouldNotPopulateCache() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        double lat = 53.9;
        double lon = 27.56;

        Update locationUpdate = createLocationUpdate(chatId, userId, lat, lon);
        UpdateContext locationCtx = UpdateContext.from(locationUpdate);

        when(searchService.findNearby(lat, lon, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT))
            .thenReturn(List.of());

        locationHandler.handle(locationUpdate, locationCtx);

        Update backUpdate = createBackUpdate(chatId, userId);
        UpdateContext backCtx = UpdateContext.from(backUpdate);
        backToListCallback.handle(backUpdate, backCtx);

        verify(sender).sendText(eq(chatId), eq(Messages.NO_TOILETS_FOUND), any(InlineKeyboardMarkup.class));
        verify(sender).sendText(eq(chatId), eq(Messages.LOCATION_REQUEST), isNull(ReplyKeyboardMarkup.class));
        verify(searchService).findNearby(lat, lon, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);
        verify(sender, never()).editOrReply(eq(backCtx), any(), any());
        assertEquals(1.0, registry.get("bot_back_list_cache_total").tag("outcome", "miss").counter().count());
    }

    private Update createLocationUpdate(long chatId, long userId, double lat, double lon) {
        Update update = new Update();
        Message message = new Message();
        message.setChat(Chat.builder().id(chatId).type("private").build());
        message.setFrom(User.builder().id(userId).isBot(false).firstName("Test").build());
        message.setLocation(Location.builder().latitude(lat).longitude(lon).build());
        update.setMessage(message);
        return update;
    }

    private Update createBackUpdate(long chatId, long userId) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("back:list");
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(Chat.builder().id(chatId).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(userId).isBot(false).firstName("Test").build());
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private NearestRestroomSlimDto sampleItem(String name, double distanceMeters) {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            distanceMeters,
            FeeType.FREE,
            new LatLon(53.9, 27.56),
            new LatLon(53.9001, 27.5601)
        );
    }
}
