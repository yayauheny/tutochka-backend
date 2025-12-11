package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.enums.FeeType;
import by.yayauheny.tutochkatgbot.TutochkaTgBotApplication;
import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.integration.WebBackendClient;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TutochkaTgBotApplication.class)
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
        Chat chat = new Chat();
        chat.setId(123L);
        message.setChat(chat);
        message.setMessageId(1);
        User from = new User();
        from.setId(10L);
        message.setFrom(from);
        Location location = new Location();
        location.setLatitude(lat);
        location.setLongitude(lon);
        message.setLocation(location);
        update.setMessage(message);
        return update;
    }

    private NearestRestroomResponseDto sampleNearest() {
        return new NearestRestroomResponseDto(
            java.util.UUID.randomUUID(),
            "Test restroom",
            "Test address",
            new by.yayauheny.shared.dto.LatLon(53.9, 27.56),
            123.0,
            FeeType.FREE,
            true,
            null,
            null,
            null
        );
    }
}
