package yayauheny.by.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "telegram.bot")
data class TelegramBotProperties(
    val enabled: Boolean = true,
    val token: String = "",
    val webhookPath: String = "/telegram/webhook",
    val webhookPublicUrl: String = "",
    val webhookSecretToken: String = "",
    val adminIds: List<String> = emptyList()
)
