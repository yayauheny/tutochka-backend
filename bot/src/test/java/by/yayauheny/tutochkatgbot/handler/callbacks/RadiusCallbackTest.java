package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;
import yayauheny.by.model.enums.FeeType;
import yayauheny.by.model.dto.Coordinates;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import by.yayauheny.tutochkatgbot.session.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RadiusCallbackTest {

    @Mock
    private MessageSender sender;
    @Mock
    private UserService userService;
    @Mock
    private SearchService searchService;
    @Mock
    private BackListSnapshotCache backListSnapshotCache;
    @Mock
    private FormatterService formatterService;
    @Mock
    private InlineKeyboardFactory inlineKeyboard;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private RadiusCallback handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RadiusCallback(sender, userService, searchService, backListSnapshotCache,
            formatterService, inlineKeyboard, replyKeyboard);
    }

    @Test
    void shouldHandleRadiusCallbackWithSessionLocation() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        int radius = 1000;
        double lat = 53.9;
        double lon = 27.56;

        Update update = createCallbackUpdate(chatId, userId, "radius:" + radius);
        UpdateContext ctx = UpdateContext.from(update);

        UserSession.Location location = new UserSession.Location(lat, lon);
        UserSession session = UserSession.withLocation(location, UserService.DEFAULT_RADIUS);
        when(userService.getSession(userId)).thenReturn(Optional.of(session));

        var mockResults =
            List.of(
                new NearestRestroomSlimDto(
                    java.util.UUID.randomUUID(),
                    "Test restroom",
                    123.0,
                    FeeType.FREE,
                    new Coordinates(lat, lon),
                    new Coordinates(lat + 0.001, lon + 0.001)
                )
            );
        when(searchService.findNearby(lat, lon, radius, SearchService.DEFAULT_NEAREST_LIMIT, ctx)).thenReturn(mockResults);
        when(formatterService.toiletsFound(1)).thenReturn("Найдено 1 туалетов поблизости:");

        handler.handle(update, ctx);

        verify(userService).getSession(userId);
        verify(searchService).findNearby(lat, lon, radius, SearchService.DEFAULT_NEAREST_LIMIT, ctx);
        verify(sender).editOrReply(eq(ctx), eq("Найдено 1 туалетов поблизости:"), any());
        verify(backListSnapshotCache).store(chatId, userId, radius, mockResults);
    }

    @Test
    void shouldRequestLocationWhenSessionMiss() throws Exception {
        long chatId = 123L;
        long userId = 456L;
        int radius = 1000;

        Update update = createCallbackUpdate(chatId, userId, "radius:" + radius);
        UpdateContext ctx = UpdateContext.from(update);

        when(userService.getSession(userId)).thenReturn(Optional.empty());

        ReplyKeyboardMarkup mockKeyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboard.shareLocation()).thenReturn(mockKeyboard);

        handler.handle(update, ctx);

        verify(userService).getSession(userId);
        verify(searchService, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt(), any(UpdateContext.class));

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(sender).editOrReply(eq(ctx), messageCaptor.capture(), isNull());
        assertEquals(Messages.LOCATION_NOT_FOUND, messageCaptor.getValue());

        verify(replyKeyboard).shareLocation();
        verify(sender).sendText(eq(chatId), eq(Messages.LOCATION_REQUEST), eq(mockKeyboard));
    }

    @Test
    void shouldHandleInvalidRadius() throws Exception {
        long chatId = 123L;
        long userId = 456L;

        Update update = createCallbackUpdate(chatId, userId, "radius:invalid");
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        verify(userService, never()).getSession(anyLong());
        verify(sender).editOrReply(eq(ctx), contains("Ошибка"), isNull());
    }

    @Test
    void shouldHandleEmptyRadius() throws Exception {
        long chatId = 123L;
        long userId = 456L;

        Update update = createCallbackUpdate(chatId, userId, "radius:");
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        verify(userService, never()).getSession(anyLong());
        verify(sender).editOrReply(eq(ctx), contains("Ошибка"), isNull());
    }

    private Update createCallbackUpdate(long chatId, long userId, String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");

        Message message = new Message();
        message.setMessageId(1);
        org.telegram.telegrambots.meta.api.objects.chat.Chat chat =
            org.telegram.telegrambots.meta.api.objects.chat.Chat.builder()
                .id(chatId)
                .type("private")
                .build();
        message.setChat(chat);
        callbackQuery.setMessage(message);

        User user = User.builder()
            .id(userId)
            .isBot(false)
            .firstName("Test")
            .build();
        callbackQuery.setFrom(user);

        update.setCallbackQuery(callbackQuery);
        return update;
    }
}
