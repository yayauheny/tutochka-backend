package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import kotlinx.serialization.json.JsonObject;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import yayauheny.by.contract.BackendClient;
import yayauheny.by.model.analytics.AnalyticsEventRequest;
import yayauheny.by.model.analytics.ProductAnalyticsEvent;

import static by.yayauheny.tutochkatgbot.messages.Messages.WELCOME_MESSAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

class StartCommandIntegrationTest extends AbstractSpringBotIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private BackendClient backendClient;

    @Test
    void startCommandSendsWelcomeWithShareLocationKeyboard() throws Exception {
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

        ArgumentCaptor<AnalyticsEventRequest> requestCaptor = ArgumentCaptor.forClass(AnalyticsEventRequest.class);
        verify(backendClient).trackProductEvent(requestCaptor.capture(), eq(10L), eq(123L), eq("test-user-10"));
        assertThat(requestCaptor.getValue().getEvent()).isEqualTo(ProductAnalyticsEvent.BOT_STARTED);
        assertThat(requestCaptor.getValue().getAcquisitionTag()).isEqualTo("direct");
        assertThat(requestCaptor.getValue().getMetadata()).isEqualTo(new JsonObject(java.util.Map.of()));
    }

    @Test
    void startCommandWithWebsitePayloadTracksWebsiteAcquisitionTag() throws Exception {
        Update update = commandUpdate(123L, 10L, "/start website");

        updateRouter.route(update);

        ArgumentCaptor<AnalyticsEventRequest> requestCaptor = ArgumentCaptor.forClass(AnalyticsEventRequest.class);
        verify(backendClient).trackProductEvent(requestCaptor.capture(), eq(10L), eq(123L), eq("test-user-10"));
        assertThat(requestCaptor.getValue().getAcquisitionTag()).isEqualTo("website");
    }
}
