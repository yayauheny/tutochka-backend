package yayauheny.by.common.config

import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
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
