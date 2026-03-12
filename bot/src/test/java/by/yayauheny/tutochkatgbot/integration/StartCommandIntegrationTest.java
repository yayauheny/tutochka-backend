package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.integration.WebBackendClient;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

import static by.yayauheny.tutochkatgbot.messages.Messages.WELCOME_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest(classes = TutochkaTgBotApplication.class)
@TestPropertySource(properties = {
        "telegram.bot.username=test_bot",
        "telegram.bot.token=test_token",
        "backend.base-url=http://localhost:9999",
        "bot.async-processing=false",
        "bot.webhook-public-url="  // Empty webhook URL to skip webhook registration in tests
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
        
        // Add MessageEntity for bot command to properly test command detection
        MessageEntity commandEntity = MessageEntity.builder()
                .type("bot_command")
                .offset(0)
                .length("/start".length())
                .build();
        message.setEntities(List.of(commandEntity));
        
        Chat chat = Chat.builder()
                .id(123L)
                .type("private")
                .build();
        message.setChat(chat);
        message.setMessageId(1);
        User from = User.builder()
                .id(10L)
                .isBot(false)
                .firstName("Test")
                .build();
        message.setFrom(from);
        update.setMessage(message);
        return update;
    }
}
