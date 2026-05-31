package yayauheny.by.common.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class TelegramWebhookConfig(
    private val telegramBotProperties: TelegramBotProperties
) {

    @Bean
    fun telegramWebhookSecretTokenFilterRegistration(): FilterRegistrationBean<TelegramWebhookSecretTokenFilter> {
        val filter = TelegramWebhookSecretTokenFilter(telegramBotProperties)
        val registration = FilterRegistrationBean(filter)
        registration.addUrlPatterns(telegramBotProperties.webhookPath)
        return registration
    }
}
