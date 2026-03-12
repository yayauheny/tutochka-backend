package by.yayauheny.tutochkatgbot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Auto-registers webhook on application startup
 */
@Component
public class WebhookRegistrar implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(WebhookRegistrar.class);
    
    private final TelegramClient client;
    
    @Value("${bot.webhook-public-url:}")
    private String webhookUrl;

    public WebhookRegistrar(TelegramClient client) {
        this.client = client;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.info("Webhook URL not configured (bot.webhook-public-url), skipping webhook registration");
            return;
        }
        
        try {
            SetWebhook setWebhook = SetWebhook.builder()
                    .url(webhookUrl)
                    .build();
            client.execute(setWebhook);
            log.info("Webhook registered successfully: {}", webhookUrl);
        } catch (TelegramApiException e) {
            log.error("Failed to register webhook: {}", webhookUrl, e);
            throw e;
        }
    }
}

