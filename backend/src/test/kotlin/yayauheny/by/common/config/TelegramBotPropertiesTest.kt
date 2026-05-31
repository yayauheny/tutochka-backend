package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration

class TelegramBotPropertiesTest {

    @Configuration
    @EnableConfigurationProperties(TelegramBotProperties::class)
    class Config

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(Config::class.java)

    @Test
    fun `loads valid configuration successfully`() {
        contextRunner.withPropertyValues(
            "telegram.bot.enabled=false",
            "telegram.bot.token=valid-token",
            "telegram.bot.webhook-secret-token=secret",
            "telegram.bot.username=my-bot",
            "telegram.bot.webhook-path=/custom/path"
        ).run { context ->
            assertThat(context).hasNotFailed()
            val properties = context.getBean(TelegramBotProperties::class.java)
            assertThat(properties.enabled).isFalse()
            assertThat(properties.token).isEqualTo("valid-token")
            assertThat(properties.webhookSecretToken).isEqualTo("secret")
            assertThat(properties.username).isEqualTo("my-bot")
            assertThat(properties.webhookPath).isEqualTo("/custom/path")
        }
    }

    @Test
    fun `loads defaults when optional fields are missing`() {
        contextRunner.withPropertyValues(
            "telegram.bot.enabled=true",
            "telegram.bot.token=valid-token",
            "telegram.bot.webhook-secret-token=secret"
        ).run { context ->
            assertThat(context).hasNotFailed()
            val properties = context.getBean(TelegramBotProperties::class.java)
            assertThat(properties.enabled).isTrue()
            assertThat(properties.token).isEqualTo("valid-token")
            assertThat(properties.webhookSecretToken).isEqualTo("secret")
            assertThat(properties.username).isEqualTo("your_bot_username")
            assertThat(properties.webhookPath).isEqualTo("/telegram/webhook")
            assertThat(properties.webhookPublicUrl).isEmpty()
            assertThat(properties.adminIds).isEmpty()
        }
    }
}
