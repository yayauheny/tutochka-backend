package yayauheny.by.bot.handler.callbacks

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.callback.CallbackData
import yayauheny.by.bot.handler.CallbackHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.service.RestroomService
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import java.util.UUID

@Component
@Order(1)
class ToiletDetailCallback(
    private val sender: MessageSender,
    private val searchService: RestroomService,
    private val analyticsService: AnalyticsService,
    private val formatterService: FormatterService,
    private val inlineKeyboard: InlineKeyboardFactory
) : CallbackHandler {
    private val log = LoggerFactory.getLogger(ToiletDetailCallback::class.java)

    override fun prefix(): String {
        return "detail"
    }

    override fun canHandle(callbackData: String): Boolean {
        return CallbackData.isType(callbackData, "detail")
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        val toiletIdStr = ctx.callbackArg()

        if (toiletIdStr.isBlank()) {
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null)
            log.warn(
                "Handled detail callback: chatId={}, userId={}, outcome=invalid_id",
                ctx.chatId,
                ctx.userId
            )
            return
        }

        val toiletId = try {
            UUID.fromString(toiletIdStr)
        } catch (e: Exception) {
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null)
            return
        }

        val toilet = searchService.getRestroomById(toiletId)
            ?: throw java.lang.IllegalArgumentException("Toilet not found: $toiletId")

        val distanceMeters = toilet.distanceMeters?.toDouble()
        val text = formatterService.toiletDetail(toilet, distanceMeters)
        sender.editOrReply(ctx, text, inlineKeyboard.toiletDetail(toilet))
        trackDetailsOpened(ctx, toiletIdStr)
        log.info(
            "Handled detail callback: chatId={}, userId={}, restroomId={}, outcome=detail_sent",
            ctx.chatId,
            ctx.userId,
            toiletIdStr
        )
    }

    private fun trackDetailsOpened(ctx: UpdateContext, restroomId: String) {
        try {
            val command = AnalyticsEventCommand(
                event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
                tgUserId = ctx.userId,
                tgChatId = ctx.chatId.toString(),
                username = ctx.username,
                source = AnalyticsSource.TELEGRAM_BOT,
                metadata = mapOf("restroom_id" to restroomId)
            )
            analyticsService.trackProductEvent(command)
        } catch (e: Exception) {
            log.warn("Failed to track restroom details analytics", e)
        }
    }
}
