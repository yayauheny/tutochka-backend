package yayauheny.by.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient
import org.telegram.telegrambots.meta.generics.TelegramClient

@Configuration
class TelegramBotRuntimeConfig {

    @Bean
    @ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
    fun telegramClient(properties: TelegramBotProperties): TelegramClient {
        require(properties.token.isNotBlank()) {
            "telegram.bot.token must be configured when bot is enabled"
        }
        return OkHttpTelegramClient(properties.token)
    }
}
