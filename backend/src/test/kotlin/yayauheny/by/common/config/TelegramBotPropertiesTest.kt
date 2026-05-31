package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.context.properties.bind.validation.BindValidationException
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
            "telegram.bot.token=valid-token",
            "telegram.bot.webhook-secret-token=secret",
            "telegram.bot.username=my-bot",
            "telegram.bot.webhook-path=/custom/path"
        ).run { context ->
            val properties = context.getBean(TelegramBotProperties::class.java)
            assertThat(properties.token).isEqualTo("valid-token")
            assertThat(properties.webhookSecretToken).isEqualTo("secret")
            assertThat(properties.username).isEqualTo("my-bot")
            assertThat(properties.webhookPath).isEqualTo("/custom/path")
        }
    }

    @Test
    fun `loads defaults when optional fields are missing`() {
        contextRunner.withPropertyValues(
            "telegram.bot.token=valid-token",
            "telegram.bot.webhook-secret-token=secret"
        ).run { context ->
            val properties = context.getBean(TelegramBotProperties::class.java)
            assertThat(properties.username).isEqualTo("your_bot_username")
            assertThat(properties.webhookPath).isEqualTo("/telegram/webhook")
            assertThat(properties.webhookPublicUrl).isEmpty()
            assertThat(properties.adminIds).isEmpty()
        }
    }

    @Test
    fun `fails to load when token is missing`() {
        contextRunner.withPropertyValues(
            "telegram.bot.webhook-secret-token=secret"
        ).run { context ->
            assertThat(context).hasFailed()
            assertThat(context.startupFailure)
                .hasRootCauseInstanceOf(NullPointerException::class.java)
                .rootCause()
                .hasMessageContaining("parameter token")
        }
    }

    @Test
    fun `fails to load when webhook secret token is missing`() {
        contextRunner.withPropertyValues(
            "telegram.bot.token=valid-token"
        ).run { context ->
            assertThat(context).hasFailed()
            assertThat(context.startupFailure)
                .hasRootCauseInstanceOf(NullPointerException::class.java)
                .rootCause()
                .hasMessageContaining("parameter webhookSecretToken")
        }
    }
}
