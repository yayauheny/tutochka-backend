package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.telegram.telegrambots.meta.generics.TelegramClient
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.NoOpMessageSender
import yayauheny.by.bot.TelegramMessageSender

class TelegramBotRuntimeConfigTest {

    @Configuration
    @EnableConfigurationProperties(TelegramBotProperties::class)
    @Import(
        TelegramBotRuntimeConfig::class,
        TelegramBotStartupValidator::class,
        TelegramWebhookRegistrar::class,
        TelegramMessageSender::class,
        NoOpMessageSender::class
    )
    class Config

    private val contextRunner = ApplicationContextRunner()
        .withUserConfiguration(Config::class.java)

    @Test
    fun `bot is enabled by default`() {
        contextRunner.withPropertyValues(
            "telegram.bot.token=valid-token",
            "telegram.bot.webhook-secret-token=secret"
        ).run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context.getBean(TelegramBotProperties::class.java).enabled).isTrue()
            assertThat(context.getBean(MessageSender::class.java)).isInstanceOf(TelegramMessageSender::class.java)
            assertThat(context.getBean(TelegramClient::class.java)).isNotNull()
        }
    }

    @Test
    fun `disabled bot starts without token or secret`() {
        contextRunner.withPropertyValues(
            "telegram.bot.enabled=false"
        ).run { context ->
            assertThat(context).hasNotFailed()
            assertThat(context.getBean(TelegramBotProperties::class.java).enabled).isFalse()
            assertThat(context).doesNotHaveBean(TelegramClient::class.java)
            assertThat(context.getBean(MessageSender::class.java)).isInstanceOf(NoOpMessageSender::class.java)
            assertThat(context.getBeanNamesForType(TelegramWebhookRegistrar::class.java)).isEmpty()
        }
    }

    @Test
    fun `enabled bot requires token`() {
        contextRunner.withPropertyValues(
            "telegram.bot.enabled=true",
            "telegram.bot.webhook-secret-token=secret"
        ).run { context ->
            assertThat(context).hasFailed()
            assertThat(context.startupFailure)
                .hasRootCauseInstanceOf(IllegalArgumentException::class.java)
                .rootCause()
                .hasMessageContaining("telegram.bot.token")
        }
    }
}
