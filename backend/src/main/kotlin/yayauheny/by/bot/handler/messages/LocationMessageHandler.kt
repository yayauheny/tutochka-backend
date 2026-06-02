package yayauheny.by.bot.handler.messages

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.cache.BackListSnapshotCache
import yayauheny.by.bot.handler.MessageHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.UserService
import yayauheny.by.service.RestroomService
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource

@Component
@Order(1)
class LocationMessageHandler(
    private val sender: MessageSender,
    private val searchService: RestroomService,
    private val analyticsService: AnalyticsService,
    private val userService: UserService,
    private val backListSnapshotCache: BackListSnapshotCache,
    private val formatterService: FormatterService,
    private val inlineKeyboard: InlineKeyboardFactory
) : MessageHandler {
    private val log = LoggerFactory.getLogger(LocationMessageHandler::class.java)

    override fun canHandle(update: Update, ctx: UpdateContext): Boolean {
        return ctx.hasLocation
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        val latitude = ctx.latitude
        val longitude = ctx.longitude

        if (latitude == null || longitude == null) {
            sender.sendText(ctx.chatId, Messages.SOMETHING_WENT_WRONG)
            log.warn("Handled location message: chatId={}, userId={}, outcome=missing_location", ctx.chatId, ctx.userId)
            return
        }

        userService.saveLocation(ctx.userId, latitude, longitude)
        val radius = UserService.DEFAULT_RADIUS

        val results = searchService.findNearestRestrooms(
            latitude = latitude,
            longitude = longitude,
            limit = 5,
            distanceMeters = radius
        )

        // Track search analytics
        analyticsService.trackNearestRestroomsSearch(
            identity = AnalyticsIdentityHeaders(
                tgUserId = ctx.userId,
                tgChatId = ctx.chatId.toString(),
                username = ctx.username,
                source = AnalyticsSource.TELEGRAM_BOT
            ),
            resultsCount = results.size,
            lat = latitude,
            lon = longitude
        )

        if (results.isEmpty()) {
            sender.sendText(ctx.chatId, Messages.NO_TOILETS_FOUND)
            log.info(
                "Handled location search: chatId={}, userId={}, radiusMeters={}, resultCount=0, outcome=no_results",
                ctx.chatId,
                ctx.userId,
                radius
            )
            return
        }

        val message = formatterService.toiletsFound(results.size)
        sender.sendText(ctx.chatId, message, inlineKeyboard.toiletList(results))
        backListSnapshotCache.store(ctx.chatId, ctx.userId, radius, results)
        log.info(
            "Handled location search: chatId={}, userId={}, radiusMeters={}, resultCount={}, outcome=results_sent",
            ctx.chatId,
            ctx.userId,
            radius,
            results.size
        )
    }
}
