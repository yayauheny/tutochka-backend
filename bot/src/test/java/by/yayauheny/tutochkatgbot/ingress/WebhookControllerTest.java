package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.service.UpdateProcessingService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.Mockito.verify;

class WebhookControllerTest {

    @Test
    void handleWebhookShouldDelegateToProcessingService() {
        UpdateProcessingService processingService = Mockito.mock(UpdateProcessingService.class);
        WebhookController controller = new WebhookController(processingService);
        Update update = new Update();

        controller.handleWebhook(update);

        verify(processingService).process(update);
    }
}
