package by.yayauheny.tutochkatgbot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Backend configuration properties
 */
@ConfigurationProperties(prefix = "backend")
public record BackendProperties(
    String baseUrl
) {}
