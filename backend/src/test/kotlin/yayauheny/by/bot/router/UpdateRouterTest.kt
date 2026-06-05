package yayauheny.by.bot.router

import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.MessageEntity
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.CallbackHandler
import yayauheny.by.bot.handler.CommandHandler
import yayauheny.by.bot.handler.MessageHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.messages.Messages

class UpdateRouterTest {
    private val commandHandler = mock(CommandHandler::class.java)
    private val firstCallbackHandler = mock(CallbackHandler::class.java)
    private val secondCallbackHandler = mock(CallbackHandler::class.java)
    private val messageHandler = mock(MessageHandler::class.java)
    private val sender = mock(MessageSender::class.java)
    private val router =
        UpdateRouter(
            listOf(commandHandler),
            listOf(firstCallbackHandler, secondCallbackHandler),
            listOf(messageHandler),
            sender
        )

    @Test
    fun `dispatches callback to first matching handler and skips others`() {
        val update = callbackUpdate("detail:123")
        val ctx = UpdateContext.from(update)

        org.mockito.Mockito.`when`(firstCallbackHandler.canHandle("detail:123")).thenReturn(true)
        org.mockito.Mockito.`when`(secondCallbackHandler.canHandle("detail:123")).thenReturn(true)

        router.route(update)

        verify(sender).answerCallbackQuery("query-id", null)
        verify(firstCallbackHandler).handle(update, ctx)
        verify(secondCallbackHandler, never()).handle(update, ctx)
        verify(commandHandler, never()).handle(update, ctx)
        verify(messageHandler, never()).handle(update, ctx)
    }

    @Test
    fun `dispatches command and skips message handlers`() {
        val update = commandUpdate("/start")
        val ctx = UpdateContext.from(update)

        org.mockito.Mockito.`when`(commandHandler.canHandle(update)).thenReturn(true)

        router.route(update)

        verify(commandHandler).handle(update, ctx)
        verify(messageHandler, never()).handle(update, ctx)
    }

    @Test
    fun `dispatches plain message to message handler`() {
        val update = textUpdate("hello")
        val ctx = UpdateContext.from(update)

        org.mockito.Mockito.`when`(messageHandler.canHandle(update, ctx)).thenReturn(true)

        router.route(update)

        verify(messageHandler).handle(update, ctx)
        verify(commandHandler, never()).handle(update, ctx)
    }

    @Test
    fun `sends fallback for unknown callback`() {
        val update = callbackUpdate("unknown:123")

        org.mockito.Mockito.`when`(firstCallbackHandler.canHandle("unknown:123")).thenReturn(false)
        org.mockito.Mockito.`when`(secondCallbackHandler.canHandle("unknown:123")).thenReturn(false)

        router.route(update)

        verify(sender).answerCallbackQuery("query-id", null)
        verify(sender).safeReply(UpdateContext.from(update), "Действие недоступно. Попробуй ещё раз или начни с /start.")
    }

    @Test
    fun `sends unknown message fallback for plain text without handlers`() {
        val update = textUpdate("hello")

        router.route(update)

        verify(sender).safeReply(UpdateContext.from(update), Messages.UNKNOWN_MESSAGE)
        verify(commandHandler, never()).handle(update, UpdateContext.from(update))
        verify(messageHandler, never()).handle(update, UpdateContext.from(update))
    }

    @Test
    fun `ignores unsupported update without message or callback`() {
        val update = Update()

        router.route(update)

        verifyNoInteractions(sender, commandHandler, firstCallbackHandler, secondCallbackHandler, messageHandler)
    }

    @Test
    fun `propagates callback handler errors`() {
        val update = callbackUpdate("detail:123")
        val ctx = UpdateContext.from(update)

        org.mockito.Mockito.`when`(firstCallbackHandler.canHandle("detail:123")).thenReturn(true)
        doThrow(RuntimeException("boom")).`when`(firstCallbackHandler).handle(update, ctx)

        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException::class.java) {
            router.route(update)
        }

        verify(sender).answerCallbackQuery("query-id", null)
        verify(secondCallbackHandler, never()).handle(update, ctx)
    }

    private fun callbackUpdate(callbackData: String): Update {
        val update = Update()
        val callbackQuery = CallbackQuery()
        callbackQuery.data = callbackData
        callbackQuery.id = "query-id"
        val message = Message()
        message.messageId = 1
        message.chat = Chat.builder().id(123L).type("private").build()
        callbackQuery.message = message
        callbackQuery.from = User.builder().id(10L).isBot(false).firstName("Test").build()
        update.callbackQuery = callbackQuery
        return update
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

    private fun textUpdate(text: String): Update {
        val update = Update()
        update.message =
            Message().apply {
                this.text = text
                chat = Chat.builder().id(123L).type("private").build()
                messageId = 1
                from = User.builder().id(10L).isBot(false).firstName("Test").build()
            }
        return update
    }
}
