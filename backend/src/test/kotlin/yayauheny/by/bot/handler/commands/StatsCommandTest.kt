package yayauheny.by.bot.handler.commands

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.telegram.telegrambots.meta.api.objects.MessageEntity
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

class StatsCommandTest {
    private val sender = mock(MessageSender::class.java)
    private val replyKeyboard = mock(ReplyKeyboardFactory::class.java)
    private val inlineKeyboard = mock(InlineKeyboardFactory::class.java)
    private val botStatsService = mock(BotStatsService::class.java)
    private val adminAccessService = BotAdminAccessService(TelegramBotProperties(adminIds = listOf("10")))
    private val handler = StatsCommand(sender, replyKeyboard, inlineKeyboard, adminAccessService, botStatsService)

    @Test
    fun `canHandle matches stats command`() {
        assertThat(handler.canHandle(commandUpdate("/stats", 10L))).isTrue()
    }

    @Test
    fun `handle sends today stats for admin`() {
        val update = commandUpdate("/stats", 10L)
        val ctx = UpdateContext.from(update)
        val keyboard = mock(InlineKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(inlineKeyboard.analytics()).thenReturn(keyboard)
        org.mockito.Mockito.`when`(botStatsService.loadToday(10L)).thenReturn("stats")

        handler.handle(update, ctx)

        verify(sender).sendText(123L, "stats", keyboard)
    }

    @Test
    fun `handle denies non admin`() {
        val update = commandUpdate("/stats", 77L)
        val ctx = UpdateContext.from(update)
        val keyboard = mock(ReplyKeyboardMarkup::class.java)

        org.mockito.Mockito.`when`(replyKeyboard.shareLocation(77L, true)).thenReturn(keyboard)

        handler.handle(update, ctx)

        verify(sender).sendText(123L, Messages.STATS_ACCESS_DENIED, keyboard)
        verifyNoInteractions(botStatsService)
    }

    @Test
    fun `handle rejects non private chat before checking admin`() {
        val update = commandUpdate("/stats", 10L, "group")
        val ctx = UpdateContext.from(update)

        handler.handle(update, ctx)

        verify(sender).sendText(123L, Messages.STATS_PRIVATE_CHAT_ONLY)
        verifyNoInteractions(replyKeyboard, botStatsService)
    }

    private fun commandUpdate(
        text: String,
        userId: Long,
        chatType: String = "private"
    ): Update {
        val update = Update()
        val message = Message()
        message.text = text
        message.chat = Chat.builder().id(123L).type(chatType).build()
        message.messageId = 1
        message.from = User.builder().id(userId).isBot(false).firstName("Test").build()
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
