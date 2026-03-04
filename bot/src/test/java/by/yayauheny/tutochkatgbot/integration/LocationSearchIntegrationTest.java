package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.location.Location;
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
        "bot.webhook-public-url="  // Empty webhook URL to skip webhook registration in tests
})
class LocationSearchIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void locationFlowReturnsList() {
        when(backendClient.findNearest(53.9, 27.56, 10, 500))
            .thenReturn(List.of(sampleNearest()));

        Update update = locationUpdate(53.9, 27.56);

        updateRouter.route(update);

        ArgumentCaptor<Long> chatCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);

        verify(messageSender).sendText(chatCaptor.capture(), textCaptor.capture(), keyboardCaptor.capture());

        assertThat(chatCaptor.getValue()).isEqualTo(123L);
        assertThat(textCaptor.getValue()).contains("Найдено").contains("1");
        assertThat(keyboardCaptor.getValue()).isNotNull();
    }

    @Test
    void locationFlowEmptyResultsSendsNoToiletsMessage() {
        when(backendClient.findNearest(53.0, 28.0, 10, 500)).thenReturn(List.of());

        Update update = locationUpdate(53.0, 28.0);

        updateRouter.route(update);

        verify(messageSender).sendText(
            org.mockito.Mockito.eq(123L),
            org.mockito.Mockito.argThat(text -> text.contains("не найдено")),
            org.mockito.Mockito.any(org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.class)
        );
    }

    private Update locationUpdate(double lat, double lon) {
        Update update = new Update();
        Message message = new Message();
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
        Location location = Location.builder()
                .latitude(lat)
                .longitude(lon)
                .build();
        message.setLocation(location);
        update.setMessage(message);
        return update;
    }

    private NearestRestroomSlimDto sampleNearest() {
        return new NearestRestroomSlimDto(
            java.util.UUID.randomUUID(),
            "Test restroom",
            123.0,
            FeeType.FREE,
            new LatLon(53.9, 27.56),
            new LatLon(53.9, 27.56)
        );
    }
}
