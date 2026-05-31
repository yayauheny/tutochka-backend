package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.boot.web.servlet.FilterRegistrationBean

class TelegramWebhookConfigTest {
    @Configuration
    @EnableConfigurationProperties(TelegramBotProperties::class)
    @Import(TelegramWebhookConfig::class)
    class Config

    private val contextRunner = ApplicationContextRunner().withUserConfiguration(Config::class.java)

    @Test
    fun `registers webhook secret filter on webhook path`() {
        contextRunner.withPropertyValues(
            "telegram.bot.enabled=true",
            "telegram.bot.token=test-token",
            "telegram.bot.webhook-secret-token=secret-token",
            "telegram.bot.webhook-path=/telegram/webhook"
        ).run { context ->
            assertThat(context).hasNotFailed()
            val registration = context.getBean("telegramWebhookSecretTokenFilterRegistration", FilterRegistrationBean::class.java)
            assertThat(registration.urlPatterns).contains("/telegram/webhook")
            assertThat(registration.filter).isInstanceOf(TelegramWebhookSecretTokenFilter::class.java)
        }
    }
}
