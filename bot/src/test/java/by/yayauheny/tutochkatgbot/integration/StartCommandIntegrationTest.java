package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import static by.yayauheny.tutochkatgbot.messages.Messages.WELCOME_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

class StartCommandIntegrationTest extends AbstractSpringBotIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void startCommandSendsWelcomeWithShareLocationKeyboard() {
        Update update = commandUpdate(123L, 10L, "/start");

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
}
