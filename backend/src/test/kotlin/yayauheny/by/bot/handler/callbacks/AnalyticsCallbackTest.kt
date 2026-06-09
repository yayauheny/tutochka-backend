package yayauheny.by.bot.handler.callbacks

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.telegram.telegrambots.meta.api.objects.CallbackQuery
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.User
import org.telegram.telegrambots.meta.api.objects.chat.Chat
import org.telegram.telegrambots.meta.api.objects.message.Message
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.bot.service.BotStatsService
import yayauheny.by.common.config.TelegramBotProperties

class AnalyticsCallbackTest {
    private val sender = mock(MessageSender::class.java)
    private val inlineKeyboard = mock(InlineKeyboardFactory::class.java)
    private val botStatsService = mock(BotStatsService::class.java)
    private val adminAccessService = BotAdminAccessService(TelegramBotProperties(adminIds = listOf("10")))
    private val handler = AnalyticsCallback(sender, adminAccessService, botStatsService, inlineKeyboard)

    @Test
    fun `canHandle matches analytics callback`() {
        assertThat(handler.canHandle("analytics:today")).isTrue()
    }

    @Test
    fun `handle sends today analytics for admin`() {
        val update = callbackUpdate("analytics:today")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(InlineKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(inlineKeyboard.analytics()).thenReturn(keyboard)
        org.mockito.Mockito.`when`(botStatsService.loadToday(10L)).thenReturn("stats")

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, "stats", keyboard)
    }

    @Test
    fun `handle sends seven day analytics for admin`() {
        val update = callbackUpdate("analytics:last_7_days")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(InlineKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(inlineKeyboard.analytics()).thenReturn(keyboard)
        org.mockito.Mockito.`when`(botStatsService.loadLast7Days(10L)).thenReturn("stats")

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, "stats", keyboard)
    }

    @Test
    fun `handle sends current month analytics for admin`() {
        val update = callbackUpdate("analytics:current_month")
        val ctx = UpdateContext.from(update)
        val keyboard = mock(InlineKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(inlineKeyboard.analytics()).thenReturn(keyboard)
        org.mockito.Mockito.`when`(botStatsService.loadCurrentMonth(10L)).thenReturn("stats")

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, "stats", keyboard)
    }

    @Test
    fun `handle returns to main menu on back`() {
        val update = callbackUpdate("analytics:back")
        val ctx = UpdateContext.from(update)

        handler.handle(update, ctx)

        verify(sender).editOrReply(ctx, Messages.LOCATION_REQUEST, null)
        verifyNoInteractions(botStatsService)
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
}
