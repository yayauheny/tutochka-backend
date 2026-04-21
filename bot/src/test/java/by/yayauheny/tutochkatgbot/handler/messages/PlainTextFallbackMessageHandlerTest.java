package by.yayauheny.tutochkatgbot.handler.messages;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.User;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PlainTextFallbackMessageHandlerTest {
    @Mock
    private MessageSender sender;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private PlainTextFallbackMessageHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new PlainTextFallbackMessageHandler(sender, replyKeyboard);
    }

    @Test
    void canHandleShouldMatchPlainTextMessages() {
        Update update = createTextUpdate("hello");
        UpdateContext ctx = UpdateContext.from(update);

        assertTrue(handler.canHandle(update, ctx));
    }

    @Test
    void handleShouldSendFallbackWithShareLocationKeyboard() throws Exception {
        Update update = createTextUpdate("hello");
        UpdateContext ctx = UpdateContext.from(update);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboard.shareLocation()).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(sender).sendText(123L, "Не понял запрос. Поделись геолокацией или набери /help", keyboard);
    }

    private Update createTextUpdate(String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        message.setChat(Chat.builder().id(123L).type("private").build());
        message.setMessageId(1);
        message.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        update.setMessage(message);
        return update;
    }
}
