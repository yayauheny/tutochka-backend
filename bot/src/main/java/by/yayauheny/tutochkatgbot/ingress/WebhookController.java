package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.config.BotModeProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Webhook controller for receiving Telegram updates
 */
@RestController
@ConditionalOnProperty(name = "bot.mode", havingValue = "WEBHOOK")
public class WebhookController {
    
    private final WebhookIngress webhookIngress;
    private final BotModeProperties botMode;
    
    public WebhookController(WebhookIngress webhookIngress, BotModeProperties botMode) {
        this.webhookIngress = webhookIngress;
        this.botMode = botMode;
    }
    
    @PostMapping("${bot.webhook-path}")
    public void handleWebhook(@RequestBody Update update) {
        webhookIngress.handleWebhook(update);
    }
}
