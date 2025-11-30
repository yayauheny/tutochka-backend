package by.yayauheny.tutochkatgbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Telegram bot configuration properties
 */
@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramProperties(
    String username,
    String token
) {}
