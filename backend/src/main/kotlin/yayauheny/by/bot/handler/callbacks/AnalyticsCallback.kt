package yayauheny.by.bot.handler.callbacks

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.callback.CallbackData
import yayauheny.by.bot.handler.CallbackHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.BotAdminAccessService
import yayauheny.by.bot.service.BotStatsPeriod
import yayauheny.by.bot.service.BotStatsService

@Component
@Order(3)
class AnalyticsCallback(
    private val sender: MessageSender,
    private val botAdminAccessService: BotAdminAccessService,
    private val botStatsService: BotStatsService,
    private val inlineKeyboard: InlineKeyboardFactory
) : CallbackHandler {
    override fun prefix(): String {
        return "analytics"
    }

    override fun canHandle(callbackData: String): Boolean {
        return CallbackData.isType(callbackData, "analytics")
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        if (!ctx.isPrivateChat) {
            sender.editOrReply(ctx, Messages.STATS_PRIVATE_CHAT_ONLY, null)
            return
        }

        if (!botAdminAccessService.isAdmin(ctx.userId)) {
            sender.editOrReply(ctx, Messages.STATS_ACCESS_DENIED, null)
            return
        }

        when (ctx.callbackArg()) {
            "back" -> sender.editOrReply(ctx, Messages.LOCATION_REQUEST, null)
            BotStatsPeriod.LAST_7_DAYS.key ->
                sender.editOrReply(ctx, botStatsService.loadLast7Days(ctx.userId), inlineKeyboard.analytics())
            BotStatsPeriod.CURRENT_MONTH.key ->
                sender.editOrReply(ctx, botStatsService.loadCurrentMonth(ctx.userId), inlineKeyboard.analytics())
            else ->
                sender.editOrReply(ctx, botStatsService.loadToday(ctx.userId), inlineKeyboard.analytics())
        }
    }
}
