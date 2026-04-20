package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BackToListCallbackTest {

    @Mock
    private MessageSender sender;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private BackListSnapshotCache cache;
    private BackToListCallback handler;
    private FormatterService formatterService;
    private InlineKeyboardFactory inlineKeyboardFactory;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatterService = new FormatterService();
        inlineKeyboardFactory = new InlineKeyboardFactory(formatterService);

        TestClock clock = new TestClock(Instant.parse("2025-01-01T00:00:00Z"));
        cache =
            BackListSnapshotCache.forTest(
                new BotMetrics(new SimpleMeterRegistry()),
                () -> TimeUnit.MILLISECONDS.toNanos(clock.millis()),
                clock,
                Duration.ofMinutes(5),
                10
            );

        handler =
            new BackToListCallback(
                sender,
                cache,
                formatterService,
                inlineKeyboardFactory,
                replyKeyboard
            );
    }

    @Test
    void handle_shouldRestoreCachedListWithoutBackend() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        Update update = createBackUpdate(chatId, userId);
        UpdateContext ctx = UpdateContext.from(update);
        List<NearestRestroomSlimDto> items = List.of(sampleItem("Test restroom"));
        cache.store(chatId, userId, 500, items);

        handler.handle(update, ctx);

        verify(sender).editOrReply(eq(ctx), eq(formatterService.toiletsFound(1)), any());
        verify(sender, never()).sendText(eq(chatId), eq(Messages.LOCATION_REQUEST), any(ReplyKeyboardMarkup.class));
    }

    @Test
    void handle_shouldAskForLocationOnCacheMiss() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        Update update = createBackUpdate(chatId, userId);
        UpdateContext ctx = UpdateContext.from(update);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboard.shareLocation()).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(sender).sendText(eq(chatId), eq(Messages.LOCATION_REQUEST), eq(keyboard));
        verify(sender, never()).editOrReply(eq(ctx), any(), any());
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

    private NearestRestroomSlimDto sampleItem(String name) {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            123.0,
            FeeType.FREE,
            new LatLon(53.9, 27.56),
            new LatLon(53.9001, 27.5601)
        );
    }

    private static final class TestClock extends Clock {
        private Instant instant;

        private TestClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        public long millis() {
            return instant.toEpochMilli();
        }
    }
}
