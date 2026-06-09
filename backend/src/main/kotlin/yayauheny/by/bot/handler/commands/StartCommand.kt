package yayauheny.by.bot.handler.commands

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.message.Message
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.CommandHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.util.CommandUtils
import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@Component
@Order(1)
class StartCommand(
    private val sender: MessageSender,
    private val replyKeyboard: ReplyKeyboardFactory,
    private val businessAnalyticsPublisher: BusinessAnalyticsPublisher
) : CommandHandler {
    override fun command(): String {
        return "/start"
    }

    override fun canHandle(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }
        val command = CommandUtils.extractCommand(update.message)
        return "/start" == command
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        trackBotStarted(update, ctx)
        sender.sendText(ctx.chatId, Messages.WELCOME_MESSAGE, replyKeyboard.shareLocation(ctx.userId, ctx.isPrivateChat))
    }

    private fun trackBotStarted(update: Update, ctx: UpdateContext) {
        val acquisitionTag = resolveAcquisitionTag(update.message)
        businessAnalyticsPublisher.trackProductEvent(
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.BOT_STARTED,
                tgUserId = ctx.userId,
                tgChatId = ctx.chatId.toString(),
                username = ctx.username,
                source = AnalyticsSource.TELEGRAM_BOT,
                acquisitionTag = acquisitionTag
            )
        )
    }

    private fun resolveAcquisitionTag(message: Message?): AcquisitionTag {
        val payload = CommandUtils.extractCommandArgument(message)
        if (payload == null) {
            return AcquisitionTag.DIRECT
        }

        return when (payload.trim().lowercase()) {
            "website" -> AcquisitionTag.WEBSITE
            "direct" -> AcquisitionTag.DIRECT
            else -> AcquisitionTag.OTHER
        }
    }
}
