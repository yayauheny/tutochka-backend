package yayauheny.by.common.config

import org.assertj.core.api.Assertions.assertThat
import io.mockk.Called
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.boot.ApplicationArguments
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient

class TelegramWebhookRegistrarTest {
    private val telegramClient = mockk<TelegramClient>()
    private val args = mockk<ApplicationArguments>(relaxed = true)

    @BeforeEach
    fun setUp() {
        clearMocks(telegramClient)
    }

    @Test
    fun `registers webhook when public url is configured`() {
        val registrar = TelegramWebhookRegistrar(
            telegramClient = telegramClient,
            properties = TelegramBotProperties(
                enabled = true,
                token = "token",
                webhookPublicUrl = "https://example.com/telegram/webhook",
                webhookSecretToken = "secret"
            )
        )

        every { telegramClient.execute(any<SetWebhook>()) } answers {
            val request = args[0] as SetWebhook
            assertThat(request.url).isEqualTo("https://example.com/telegram/webhook")
            assertThat(request.secretToken).isEqualTo("secret")
            mockk()
        }

        registrar.run(args)

        verify(exactly = 1) { telegramClient.execute(any<SetWebhook>()) }
    }

    @Test
    fun `skips webhook registration when public url is blank`() {
        val registrar = TelegramWebhookRegistrar(
            telegramClient = telegramClient,
            properties = TelegramBotProperties(
                enabled = true,
                token = "token",
                webhookPublicUrl = "",
                webhookSecretToken = "secret"
            )
        )

        registrar.run(args)

        verify { telegramClient wasNot Called }
    }

    @Test
    fun `fails when Telegram registration fails`() {
        val registrar = TelegramWebhookRegistrar(
            telegramClient = telegramClient,
            properties = TelegramBotProperties(
                enabled = true,
                token = "token",
                webhookPublicUrl = "https://example.com/telegram/webhook",
                webhookSecretToken = "secret"
            )
        )

        every { telegramClient.execute(any<SetWebhook>()) } throws TelegramApiException("boom")

        assertThrows<TelegramApiException> {
            registrar.run(args)
        }
    }
}
