package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HelpCommandTest {
    @Mock
    private MessageSender sender;
    @Mock
    private ReplyKeyboardFactory replyKeyboard;

    private HelpCommand handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new HelpCommand(sender, replyKeyboard);
    }

    @Test
    void canHandleShouldMatchHelpCommand() {
        Update update = createCommandUpdate("/help");

        assertTrue(handler.canHandle(update));
    }

    @Test
    void handleShouldSendHelpMessageWithKeyboard() throws Exception {
        Update update = createCommandUpdate("/help");
        UpdateContext ctx = UpdateContext.from(update);
        ReplyKeyboardMarkup keyboard = mock(ReplyKeyboardMarkup.class);
        when(replyKeyboard.helpAndLocation()).thenReturn(keyboard);

        handler.handle(update, ctx);

        verify(sender).sendText(123L, Messages.HELP_MESSAGE, keyboard);
    }

    private Update createCommandUpdate(String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        message.setChat(Chat.builder().id(123L).type("private").build());
        message.setMessageId(1);
        message.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        message.setEntities(
            List.of(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length(text.length())
                    .build()
            )
        );
        update.setMessage(message);
        return update;
    }
}
