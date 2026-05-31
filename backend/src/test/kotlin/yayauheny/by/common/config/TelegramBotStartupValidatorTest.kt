package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThatCode
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.boot.DefaultApplicationArguments

class TelegramBotStartupValidatorTest {
    @Test
    fun `accepts configured webhook secret`() {
        val validator = TelegramBotStartupValidator(
            TelegramBotProperties(
                enabled = true,
                token = "token",
                webhookSecretToken = "secret"
            )
        )

        assertThatCode {
            validator.run(DefaultApplicationArguments())
        }.doesNotThrowAnyException()
    }

    @Test
    fun `rejects missing webhook secret`() {
        val validator = TelegramBotStartupValidator(
            TelegramBotProperties(
                enabled = true,
                token = "token",
                webhookSecretToken = ""
            )
        )

        assertThatThrownBy {
            validator.run(DefaultApplicationArguments())
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("telegram.bot.webhook-secret-token")
    }
}
