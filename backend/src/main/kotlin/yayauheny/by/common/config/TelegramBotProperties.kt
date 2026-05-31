package yayauheny.by.common.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@Validated
@ConfigurationProperties(prefix = "telegram.bot")
data class TelegramBotProperties(
    val username: String = "your_bot_username",
    @field:NotBlank(message = "Telegram bot token must not be blank")
    val token: String,
    val webhookPath: String = "/telegram/webhook",
    val webhookPublicUrl: String = "",
    @field:NotBlank(message = "Telegram webhook secret token must not be blank")
    val webhookSecretToken: String,
    val adminIds: List<String> = emptyList()
)
