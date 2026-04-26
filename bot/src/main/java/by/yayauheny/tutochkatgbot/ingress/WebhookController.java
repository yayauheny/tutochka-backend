package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.service.UpdateProcessingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
public class WebhookController {
    private final UpdateProcessingService processingService;
    
    public WebhookController(UpdateProcessingService processingService) {
        this.processingService = processingService;
    }
    
    @PostMapping("${bot.webhook-path}")
    public void handleWebhook(@RequestBody Update update) throws Exception {
        processingService.process(update);
    }
}
