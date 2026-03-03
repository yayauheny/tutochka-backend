package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
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
    private FormatterService formatterService;
    @Mock
    private InlineKeyboardFactory inlineKeyboard;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private RadiusCallback handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new RadiusCallback(sender, userService, searchService,
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

        var mockResults = List.<NearestRestroomSlimDto>of();
        when(searchService.findNearby(lat, lon, radius, 10)).thenReturn(mockResults);
        when(formatterService.toiletsFound(0)).thenReturn("Найдено 0 туалетов");
        when(inlineKeyboard.radiusSelection()).thenReturn(null);

        handler.handle(update, ctx);

        verify(userService).getSession(userId);
        verify(searchService).findNearby(lat, lon, radius, 10);
        verify(sender).editOrReply(eq(ctx), anyString(), any());
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
        verify(searchService, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt());

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
