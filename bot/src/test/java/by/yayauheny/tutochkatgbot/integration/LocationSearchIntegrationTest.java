package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import by.yayauheny.tutochkatgbot.service.SearchService;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocationSearchIntegrationTest extends AbstractSpringBotIntegrationTest {

    @Autowired
    private UpdateRouter updateRouter;

    @Autowired
    private MeterRegistry meterRegistry;

    @MockitoBean
    private MessageSender messageSender;

    @MockitoBean
    private WebBackendClient backendClient;

    @Test
    void locationFlowReturnsList() {
        when(backendClient.findNearest(53.9, 27.56, SearchService.DEFAULT_NEAREST_LIMIT, 500))
            .thenReturn(List.of(nearestRestroom("Test restroom", 123.0)));

        Update update = locationUpdate(123L, 10L, 53.9, 27.56);

        updateRouter.route(update);

        ArgumentCaptor<Long> chatCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);

        verify(messageSender).sendText(chatCaptor.capture(), textCaptor.capture(), keyboardCaptor.capture());

        assertThat(chatCaptor.getValue()).isEqualTo(123L);
        assertThat(textCaptor.getValue()).contains("Найдено").contains("1");
        assertThat(keyboardCaptor.getValue()).isNotNull();

        double backendCalls = meterRegistry.get("bot_backend_requests_total")
            .tag("endpoint", "/restrooms/nearest")
            .tag("outcome", "success")
            .counter()
            .count();
        long durationCount = meterRegistry.get("bot_backend_request_duration_seconds")
            .tag("endpoint", "/restrooms/nearest")
            .timer()
            .count();

        assertThat(backendCalls).isGreaterThanOrEqualTo(1.0);
        assertThat(durationCount).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void locationFlowEmptyResultsSendsNoToiletsMessage() {
        when(backendClient.findNearest(53.0, 28.0, SearchService.DEFAULT_NEAREST_LIMIT, 500))
            .thenReturn(List.of());

        Update update = locationUpdate(123L, 10L, 53.0, 28.0);

        updateRouter.route(update);

        verify(messageSender).sendText(
            org.mockito.Mockito.eq(123L),
            org.mockito.Mockito.argThat(text -> text.contains("не найдено")),
            org.mockito.Mockito.any(org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup.class)
        );
    }
}
