package by.yayauheny.tutochkatgbot.ingress;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Webhook controller for receiving Telegram updates
 */
@RestController
public class WebhookController {
    
    private final TutochkaWebhookBot bot;
    
    public WebhookController(TutochkaWebhookBot bot) {
        this.bot = bot;
    }
    
    @PostMapping("${bot.webhook-path:/telegram/webhook}")
    public BotApiMethod<?> handleWebhook(@RequestBody Update update) {
        return bot.onWebhookUpdateReceived(update);
    }
}
