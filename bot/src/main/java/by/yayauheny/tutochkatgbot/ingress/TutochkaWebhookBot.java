package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.config.TelegramProperties;
import by.yayauheny.tutochkatgbot.service.UpdateProcessingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Webhook bot implementation using TelegramWebhookBot
 */
@Component
public class TutochkaWebhookBot extends TelegramWebhookBot {
    
    private final TelegramProperties telegramProperties;
    private final UpdateProcessingService updateProcessingService;
    private final String webhookPath;
    
    public TutochkaWebhookBot(
            TelegramProperties telegramProperties,
            UpdateProcessingService updateProcessingService,
            @Value("${bot.webhook-path:/telegram/webhook}") String webhookPath
    ) {
        this.telegramProperties = telegramProperties;
        this.updateProcessingService = updateProcessingService;
        this.webhookPath = webhookPath;
    }
    
    @Override
    public String getBotUsername() {
        return telegramProperties.username();
    }
    
    @Override
    public String getBotToken() {
        return telegramProperties.token();
    }
    
    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        // Process update asynchronously to respond quickly to Telegram
        // This prevents Telegram from retrying/duplicating requests
        updateProcessingService.processUpdateAsync(update);
        return null;
    }
    
    @Override
    public String getBotPath() {
        return webhookPath;
    }
}
