package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.dto.backend.AccessibilityType;
import by.yayauheny.tutochkatgbot.dto.backend.DataSourceType;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.ImportProvider;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.LocationType;
import by.yayauheny.tutochkatgbot.dto.backend.PlaceType;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomStatus;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.util.Links;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TutochkaTgBotApplication.class)
@TestPropertySource(properties = {
    "telegram.bot.username=test_bot",
    "telegram.bot.token=test_token",
    "backend.base-url=http://localhost:9999",
    "bot.async-processing=false",
    "bot.webhook-public-url="
})
class DetailFlowIntegrationTest {
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
            .thenReturn(Optional.of(sampleRestroom(restroomId)));

        Update update = detailUpdate(restroomId);

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

    private Update detailUpdate(UUID restroomId) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("detail:" + restroomId);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(Chat.builder().id(123L).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private RestroomResponseDto sampleRestroom(UUID restroomId) {
        return new RestroomResponseDto(
            restroomId,
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            Map.of(),
            Map.of(),
            FeeType.FREE,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new LatLon(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            Map.of(),
            Map.of("2gis", (Object) "abc123"),
            null,
            null,
            false,
            false,
            LocationType.UNKNOWN,
            ImportProvider.USER,
            null,
            false,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-01T00:00:00Z"),
            100,
            null,
            null
        );
    }
}
