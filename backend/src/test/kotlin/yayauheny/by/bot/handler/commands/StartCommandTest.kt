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
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

class StartCommandTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val businessAnalyticsPublisher = mock(BusinessAnalyticsPublisher::class.java)
    private val handler = StartCommand(sender, replyKeyboard, businessAnalyticsPublisher)

    @Test
    fun `canHandle matches start command`() {
        assertThat(handler.canHandle(commandUpdate("/start"))).isTrue()
    }

    @Test
    fun `handle sends welcome message and tracks acquisition tag`() {
        val update = commandUpdate("/start website")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)
        org.mockito.Mockito.`when`(replyKeyboard.shareLocation()).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(businessAnalyticsPublisher).trackProductEvent(
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.BOT_STARTED,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user-10",
                source = AnalyticsSource.TELEGRAM_BOT,
                acquisitionTag = AcquisitionTag.WEBSITE
            )
        )
        verify(sender).sendText(123L, Messages.WELCOME_MESSAGE, keyboard)
    }

    private fun commandUpdate(text: String): Update {
        val update = Update()
        val message = Message()
        message.text = text
        message.chat = Chat.builder().id(123L).type("private").build()
        message.messageId = 1
        message.from = User.builder().id(10L).isBot(false).firstName("Test").userName("test-user-10").build()
        message.entities =
            listOf(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length("/start".length)
                    .build()
            )
        update.message = message
        return update
    }
}
