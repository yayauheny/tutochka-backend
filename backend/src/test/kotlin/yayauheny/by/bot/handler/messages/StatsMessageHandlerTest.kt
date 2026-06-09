package yayauheny.by.bot.handler.messages

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.bot.service.BotStatsService
import yayauheny.by.common.config.TelegramBotProperties

class StatsMessageHandlerTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val inlineKeyboard = mock(InlineKeyboardFactory::class.java)
    private val botStatsService = mock(BotStatsService::class.java)
    private val adminAccessService = BotAdminAccessService(TelegramBotProperties(adminIds = listOf("10")))
    private val handler = StatsMessageHandler(sender, replyKeyboard, inlineKeyboard, adminAccessService, botStatsService)

    @Test
    fun `canHandle matches stats buttons`() {
        val update = textUpdate(Messages.BUTTON_ANALYTICS, 10L)
        val ctx = UpdateContext.from(update)

        assertThat(handler.canHandle(update, ctx)).isTrue()
    }

    @Test
    fun `handle sends today analytics for admin`() {
        val update = textUpdate(Messages.BUTTON_ANALYTICS, 10L)
        val ctx = UpdateContext.from(update)
        val keyboard = mock(InlineKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(inlineKeyboard.analytics()).thenReturn(keyboard)
        org.mockito.Mockito.`when`(botStatsService.loadToday(10L)).thenReturn("stats")

        handler.handle(update, ctx)

        verify(sender).sendText(123L, "stats", keyboard)
    }

    @Test
    fun `handle denies non admin`() {
        val update = textUpdate(Messages.BUTTON_ANALYTICS, 99L)
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(replyKeyboard.shareLocation(99L, true)).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(sender).sendText(123L, Messages.STATS_ACCESS_DENIED, keyboard)
        verifyNoInteractions(botStatsService)
    }

    @Test
    fun `canHandle ignores stats buttons outside private chat`() {
        val update = textUpdate(Messages.BUTTON_STATS_CURRENT_MONTH, 10L, "group")
        val ctx = UpdateContext.from(update)

        assertThat(handler.canHandle(update, ctx)).isFalse()
    }

    private fun textUpdate(
        text: String,
        userId: Long,
        chatType: String = "private"
    ): Update {
        val update = Update()
        update.message =
            Message().apply {
                this.text = text
                chat = Chat.builder().id(123L).type(chatType).build()
                messageId = 1
                from = User.builder().id(userId).isBot(false).firstName("Test").build()
            }
        return update
    }
}
