package yayauheny.by.bot

import org.assertj.core.api.Assertions.assertThat
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient
import yayauheny.by.bot.handler.UpdateContext

class TelegramMessageSenderTest {
    private val telegramClient = mockk<TelegramClient>()
    private val sender = TelegramMessageSender(telegramClient)

    @BeforeEach
    fun setUp() {
        clearMocks(telegramClient)
    }

    @Test
    fun `sends text message as HTML`() {
        val replyKeyboard = mockk<ReplyKeyboardMarkup>()
        every { telegramClient.execute(any<SendMessage>()) } answers {
            val request = args[0] as SendMessage
            assertThat(request.chatId).isEqualTo("42")
            assertThat(request.text).isEqualTo("<b>Hello</b>")
            assertThat(request.parseMode).isEqualTo("HTML")
            assertThat(request.replyMarkup).isSameAs(replyKeyboard)
            mockk()
        }

        sender.sendText(42L, "<b>Hello</b>", replyKeyboard)
        verify(exactly = 1) { telegramClient.execute(any<SendMessage>()) }
    }

    @Test
    fun `edits message when message id is present`() {
        every { telegramClient.execute(any<EditMessageText>()) } answers {
            val request = args[0] as EditMessageText
            assertThat(request.chatId).isEqualTo("42")
            assertThat(request.messageId).isEqualTo(99)
            assertThat(request.text).isEqualTo("Updated")
            assertThat(request.parseMode).isEqualTo("HTML")
            mockk()
        }
        val ctx = UpdateContext(
            chatId = 42L,
            userId = 7L,
            username = "tester",
            text = null,
            hasLocation = false,
            latitude = null,
            longitude = null,
            isCallback = true,
            callbackData = "detail:1",
            messageId = 99
        )

        sender.editMessage(ctx, "Updated", null)
        verify(exactly = 1) { telegramClient.execute(any<EditMessageText>()) }
    }

    @Test
    fun `falls back to send when edit fails`() {
        every { telegramClient.execute(any<EditMessageText>()) } throws TelegramApiException("boom")
        every { telegramClient.execute(any<SendMessage>()) } returns mockk()
        val ctx = UpdateContext(
            chatId = 42L,
            userId = 7L,
            username = "tester",
            text = null,
            hasLocation = false,
            latitude = null,
            longitude = null,
            isCallback = true,
            callbackData = "detail:1",
            messageId = 99
        )

        sender.editMessage(ctx, "Updated", null)
        verify(exactly = 1) { telegramClient.execute(any<EditMessageText>()) }
        verify(exactly = 1) { telegramClient.execute(any<SendMessage>()) }
    }

    @Test
    fun `answers callback query with truncated text`() {
        every { telegramClient.execute(any<AnswerCallbackQuery>()) } answers {
            val request = args[0] as AnswerCallbackQuery
            assertThat(request.callbackQueryId).isEqualTo("callback-id")
            assertThat(request.text).hasSize(200)
            mockk()
        }

        sender.answerCallbackQuery("callback-id", "x".repeat(250))
        verify(exactly = 1) { telegramClient.execute(any<AnswerCallbackQuery>()) }
    }

    @Test
    fun `wraps telegram api failures on send`() {
        every { telegramClient.execute(any<SendMessage>()) } throws TelegramApiException("boom")

        assertThatThrownBy {
            sender.sendMessage(
                SendMessage.builder()
                    .chatId("42")
                    .text("Hello")
                    .build()
            )
        }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessageContaining("Failed to send message to chat 42")
    }

    @Test
    fun `safe reply skips invalid chat id`() {
        val ctx = UpdateContext(
            chatId = 0L,
            userId = 7L,
            username = "tester",
            text = null,
            hasLocation = false,
            latitude = null,
            longitude = null,
            isCallback = false,
            callbackData = null,
            messageId = null
        )

        sender.safeReply(ctx, "Hello")

        verify(exactly = 0) { telegramClient.execute(any<SendMessage>()) }
    }

    @Test
    fun `rejects invalid chat id on send`() {
        assertThatThrownBy {
            sender.sendText(0L, "Hello")
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessageContaining("Chat id must be positive")
    }
}
