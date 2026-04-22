package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.util.Links;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DetailFlowIntegrationTest extends AbstractSpringBotIntegrationTest {
    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void detailCallbackShouldReturnCardWithUrlKeyboard() {
        UUID restroomId = UUID.randomUUID();
        when(backendClient.getById(restroomId.toString()))
            .thenReturn(Optional.of(restroomResponse(restroomId, twoGisExternalMap("abc123"))));

        Update update = callbackUpdate(123L, 10L, CallbackData.detail(restroomId.toString()));

        updateRouter.route(update);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);

        verify(messageSender).answerCallbackQuery("query-id", null);
        verify(messageSender).editOrReply(org.mockito.ArgumentMatchers.any(UpdateContext.class), textCaptor.capture(), keyboardCaptor.capture());

        assertThat(textCaptor.getValue()).contains("Test restroom");
        assertThat(hasExpectedButtons(keyboardCaptor.getValue())).isTrue();
    }

    private boolean hasExpectedButtons(InlineKeyboardMarkup keyboard) {
        return keyboard != null
            && keyboard.getKeyboard().stream().flatMap(row -> row.stream()).anyMatch(button -> Links.twoGisById("abc123").equals(button.getUrl()))
            && keyboard.getKeyboard().stream().flatMap(row -> row.stream()).anyMatch(button -> "back:list".equals(button.getCallbackData()));
    }
}
