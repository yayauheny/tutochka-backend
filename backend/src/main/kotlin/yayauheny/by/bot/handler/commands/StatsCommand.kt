package yayauheny.by.bot.handler.commands

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.CommandHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.bot.service.BotStatsService
import yayauheny.by.bot.util.CommandUtils

@Component
@Order(3)
class StatsCommand(
    private val sender: MessageSender,
    private val replyKeyboard: ReplyKeyboardFactory,
    private val inlineKeyboard: InlineKeyboardFactory,
    private val botAdminAccessService: BotAdminAccessService,
    private val botStatsService: BotStatsService
) : CommandHandler {
    override fun command(): String = "/stats"

    override fun canHandle(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }
        val command = CommandUtils.extractCommand(update.message)
        return "/stats".equals(command, ignoreCase = true)
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

        sender.sendText(ctx.chatId, botStatsService.loadToday(ctx.userId), inlineKeyboard.analytics())
    }
}
