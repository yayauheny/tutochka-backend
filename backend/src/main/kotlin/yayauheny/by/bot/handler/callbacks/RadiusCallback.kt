package yayauheny.by.bot.handler.callbacks

import org.slf4j.LoggerFactory
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.cache.BackListSnapshotCache
import yayauheny.by.bot.callback.CallbackData
import yayauheny.by.bot.handler.CallbackHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.InlineKeyboardFactory
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.service.FormatterService
import yayauheny.by.bot.service.UserService
import yayauheny.by.service.RestroomService
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource

@Component
@Order(3)
class RadiusCallback(
    private val sender: MessageSender,
    private val userService: UserService,
    private val searchService: RestroomService,
    private val analyticsService: AnalyticsService,
    private val backListSnapshotCache: BackListSnapshotCache,
    private val formatterService: FormatterService,
    private val inlineKeyboard: InlineKeyboardFactory,
    private val replyKeyboard: ReplyKeyboardFactory
) : CallbackHandler {
    private val log = LoggerFactory.getLogger(RadiusCallback::class.java)

    override fun prefix(): String {
        return "radius"
    }

    override fun canHandle(callbackData: String): Boolean {
        return CallbackData.isType(callbackData, "radius")
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        try {
            val radiusStr = ctx.callbackArg()
            if (radiusStr.isBlank()) {
                sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null)
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, outcome=invalid_radius",
                    ctx.chatId,
                    ctx.userId
                )
                return
            }

            val radius = radiusStr.toInt()
            if (radius <= 0) {
                sender.editOrReply(ctx, "❌ Ошибка: радиус должен быть больше 0", null)
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, outcome=invalid_radius",
                    ctx.chatId,
                    ctx.userId,
                    radius
                )
                return
            }

            val sessionOpt = userService.getSession(ctx.userId)
            if (sessionOpt.isEmpty || sessionOpt.get().location == null) {
                sender.editOrReply(ctx, Messages.LOCATION_NOT_FOUND, null)
                sender.sendText(ctx.chatId, Messages.LOCATION_REQUEST, replyKeyboard.shareLocation())
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, outcome=missing_location",
                    ctx.chatId,
                    ctx.userId,
                    radius
                )
                return
            }

            val location = sessionOpt.get().location!!
            userService.updateRadius(ctx.userId, radius)
            val results = searchService.findNearestRestrooms(
                latitude = location.latitude,
                longitude = location.longitude,
                limit = 5, // DEFAULT_NEAREST_LIMIT
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
                lat = location.latitude,
                lon = location.longitude
            )

            if (results.isEmpty()) {
                sender.editOrReply(ctx, Messages.NO_TOILETS_FOUND, inlineKeyboard.radiusSelection())
                log.info(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, resultCount=0, outcome=no_results",
                    ctx.chatId,
                    ctx.userId,
                    radius
                )
                return
            }

            val message = formatterService.toiletsFound(results.size)
            sender.editOrReply(ctx, message, inlineKeyboard.toiletList(results))
            backListSnapshotCache.store(ctx.chatId, ctx.userId, radius, results)
            log.info(
                "Handled radius callback: chatId={}, userId={}, radiusMeters={}, resultCount={}, outcome=results_sent",
                ctx.chatId,
                ctx.userId,
                radius,
                results.size
            )
        } catch (e: NumberFormatException) {
            sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null)
            log.warn(
                "Handled radius callback: chatId={}, userId={}, outcome=invalid_radius",
                ctx.chatId,
                ctx.userId
            )
        }
    }
}
