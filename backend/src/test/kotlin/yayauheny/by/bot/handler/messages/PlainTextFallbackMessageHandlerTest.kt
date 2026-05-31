package yayauheny.by.bot.handler.messages

import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory

class PlainTextFallbackMessageHandlerTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val handler = PlainTextFallbackMessageHandler(sender, replyKeyboard)

    @Test
    fun `canHandle matches plain text messages`() {
        val update = textUpdate("hello")
        val ctx = UpdateContext.from(update)

        org.assertj.core.api.Assertions.assertThat(handler.canHandle(update, ctx)).isTrue()
    }

    @Test
    fun `handle sends fallback with share location keyboard`() {
        val update = textUpdate("hello")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)
        org.mockito.Mockito.`when`(replyKeyboard.shareLocation()).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(sender).sendText(123L, "Не понял запрос. Поделись геолокацией или набери /help", keyboard)
    }

    private fun textUpdate(text: String): Update {
        val update = Update()
        val message = Message()
        message.text = text
        message.chat = Chat.builder().id(123L).type("private").build()
        message.messageId = 1
        message.from = User.builder().id(10L).isBot(false).firstName("Test").build()
        update.message = message
        return update
    }
}
