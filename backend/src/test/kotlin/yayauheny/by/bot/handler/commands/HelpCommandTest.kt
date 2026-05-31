package yayauheny.by.bot.handler.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages

class HelpCommandTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val handler = HelpCommand(sender, replyKeyboard)

    @Test
    fun `canHandle matches help command`() {
        assertThat(handler.canHandle(commandUpdate("/help"))).isTrue()
    }

    @Test
    fun `handle sends help message with keyboard`() {
        val update = commandUpdate("/help")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)
        org.mockito.Mockito.`when`(replyKeyboard.helpAndLocation()).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(sender).sendText(123L, Messages.HELP_MESSAGE, keyboard)
    }

    private fun commandUpdate(text: String): Update {
        val update = Update()
        val message = Message()
        message.text = text
        message.chat = Chat.builder().id(123L).type("private").build()
        message.messageId = 1
        message.from = User.builder().id(10L).isBot(false).firstName("Test").build()
        message.entities =
            listOf(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length(text.length)
                    .build()
            )
        update.message = message
        return update
    }
}
