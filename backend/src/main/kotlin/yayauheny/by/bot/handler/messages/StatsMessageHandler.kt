package yayauheny.by.bot.handler.messages

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.MessageHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.bot.service.BotStatsService
import yayauheny.by.bot.util.CommandUtils

@Component
@Order(2)
class StatsMessageHandler(
    private val sender: MessageSender,
    private val replyKeyboard: ReplyKeyboardFactory,
    private val inlineKeyboard: InlineKeyboardFactory,
    private val botAdminAccessService: BotAdminAccessService,
    private val botStatsService: BotStatsService
) : MessageHandler {
    override fun canHandle(update: Update, ctx: UpdateContext): Boolean {
        return update.hasMessage() &&
            ctx.isPrivateChat &&
            !CommandUtils.isCommand(update.message) &&
            ctx.text in handledButtons
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        if (!ctx.isPrivateChat) {
            sender.sendText(ctx.chatId, Messages.STATS_PRIVATE_CHAT_ONLY)
            return
        }

        if (!botAdminAccessService.isAdmin(ctx.userId)) {
            sender.sendText(ctx.chatId, Messages.STATS_ACCESS_DENIED, replyKeyboard.shareLocation(ctx.userId, true))
            return
        }

        val text =
            when (ctx.text) {
                Messages.BUTTON_ANALYTICS, Messages.BUTTON_STATS_TODAY -> botStatsService.loadToday(ctx.userId)
                Messages.BUTTON_STATS_LAST_7_DAYS -> botStatsService.loadLast7Days(ctx.userId)
                Messages.BUTTON_STATS_CURRENT_MONTH -> botStatsService.loadCurrentMonth(ctx.userId)
                Messages.BUTTON_STATS_REFRESH -> botStatsService.reloadLastOrToday(ctx.userId)
                else -> return
            }

        sender.sendText(ctx.chatId, text, inlineKeyboard.analytics())
    }

    companion object {
        private val handledButtons =
            setOf(
                Messages.BUTTON_ANALYTICS,
                Messages.BUTTON_STATS_TODAY,
                Messages.BUTTON_STATS_LAST_7_DAYS,
                Messages.BUTTON_STATS_CURRENT_MONTH,
                Messages.BUTTON_STATS_REFRESH
            )
    }
}
