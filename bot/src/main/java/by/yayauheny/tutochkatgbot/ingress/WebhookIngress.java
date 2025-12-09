package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Webhook implementation of UpdateIngress
 */
@Component
@ConditionalOnProperty(name = "bot.mode", havingValue = "WEBHOOK")
public class WebhookIngress {
    
    private final UpdateRouter router;
    
    public WebhookIngress(UpdateRouter router) {
        this.router = router;
    }
    
    /**
     * Handle incoming webhook update
     * @param update Telegram update
     */
    public void handleWebhook(Update update) {
        router.route(update);
    }
}
