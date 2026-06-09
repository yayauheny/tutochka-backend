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

@Component
@Order(2)
class BackToListCallback(
    private val sender: MessageSender,
    private val backListSnapshotCache: BackListSnapshotCache,
    private val formatterService: FormatterService,
    private val inlineKeyboard: InlineKeyboardFactory,
    private val replyKeyboard: ReplyKeyboardFactory
) : CallbackHandler {
    private val log = LoggerFactory.getLogger(BackToListCallback::class.java)

    override fun prefix(): String {
        return "back"
    }

    override fun canHandle(callbackData: String): Boolean {
        return CallbackData.isType(callbackData, "back") && "list" == CallbackData.arg(callbackData)
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        val snapshotOpt = backListSnapshotCache.get(ctx.chatId, ctx.userId)
        if (snapshotOpt.isEmpty) {
            sender.sendText(ctx.chatId, Messages.LOCATION_REQUEST, replyKeyboard.shareLocation(ctx.userId, ctx.isPrivateChat))
            log.warn(
                "Handled back-to-list callback: chatId={}, userId={}, outcome=location_requested",
                ctx.chatId,
                ctx.userId
            )
            return
        }

        val snapshot = snapshotOpt.get()
        val message = formatterService.toiletsFound(snapshot.items.size)
        sender.editOrReply(ctx, message, inlineKeyboard.toiletList(snapshot.items))
        log.info(
            "Handled back-to-list callback: chatId={}, userId={}, itemCount={}, outcome=snapshot_restored",
            ctx.chatId,
            ctx.userId,
            snapshot.items.size
        )
    }
}
