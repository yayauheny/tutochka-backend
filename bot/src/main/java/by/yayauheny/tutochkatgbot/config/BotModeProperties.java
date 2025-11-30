package by.yayauheny.tutochkatgbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Bot mode configuration properties
 */
@ConfigurationProperties(prefix = "bot")
public record BotModeProperties(
    String mode,
    String webhookPath,
    String webhookPublicUrl
) {}
