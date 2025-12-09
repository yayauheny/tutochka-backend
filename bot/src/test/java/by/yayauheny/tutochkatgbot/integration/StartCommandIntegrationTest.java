package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.integration.WebBackendClient;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockitoBean;
import org.springframework.test.context.TestPropertySource;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import static by.yayauheny.tutochkatgbot.messages.Messages.WELCOME_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TutochkaTgBotApplication.class)
@TestPropertySource(properties = {
    "telegram.bot.username=test_bot",
    "telegram.bot.token=test_token",
    "backend.base-url=http://localhost:9999"
})
class StartCommandIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void startCommandSendsWelcomeWithShareLocationKeyboard() {
        Update update = startUpdate();

        updateRouter.route(update);

        ArgumentCaptor<Long> chatCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<ReplyKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(ReplyKeyboardMarkup.class);

        verify(messageSender).sendText(chatCaptor.capture(), textCaptor.capture(), keyboardCaptor.capture());

        assertThat(chatCaptor.getValue()).isEqualTo(123L);
        assertThat(textCaptor.getValue()).isEqualTo(WELCOME_MESSAGE);
        assertThat(keyboardCaptor.getValue()).isNotNull();
        assertThat(keyboardCaptor.getValue().getKeyboard()).isNotEmpty();
    }

    private Update startUpdate() {
        Update update = new Update();
        Message message = new Message();
        message.setText("/start");
        Chat chat = new Chat();
        chat.setId(123L);
        message.setChat(chat);
        message.setMessageId(1);
        User from = new User();
        from.setId(10L);
        from.setFirstName("Test");
        message.setFrom(from);
        update.setMessage(message);
        return update;
    }
}
