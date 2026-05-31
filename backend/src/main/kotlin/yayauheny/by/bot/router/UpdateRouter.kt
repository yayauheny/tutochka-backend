package yayauheny.by.bot.router

import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.CallbackHandler
import yayauheny.by.bot.handler.CommandHandler
import yayauheny.by.bot.handler.MessageHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.util.CommandUtils

@Component
class UpdateRouter(
    commandHandlers: List<CommandHandler>,
    callbackHandlers: List<CallbackHandler>,
    messageHandlers: List<MessageHandler>,
    private val sender: MessageSender
) {
    private val commandHandlers = commandHandlers.sortedWith(AnnotationAwareOrderComparator.INSTANCE)
    private val callbackHandlers = callbackHandlers.sortedWith(AnnotationAwareOrderComparator.INSTANCE)
    private val messageHandlers = messageHandlers.sortedWith(AnnotationAwareOrderComparator.INSTANCE)

    @Throws(Exception::class)
    fun route(update: Update) {
        val ctx = UpdateContext.from(update)

        if (update.hasCallbackQuery()) {
            sender.answerCallbackQuery(update.callbackQuery.id, null)
            handleCallback(update, ctx)
            return
        }

        if (update.hasMessage() && CommandUtils.isCommand(update.message)) {
            handleCommand(update, ctx)
            return
        }

        handleMessage(update, ctx)
    }

    @Throws(Exception::class)
    private fun handleCallback(update: Update, ctx: UpdateContext) {
        val callbackData = ctx.callbackData ?: return

        for (handler in callbackHandlers) {
            if (handler.canHandle(callbackData)) {
                handler.handle(update, ctx)
                return
            }
        }

        sender.safeReply(ctx, "Действие недоступно. Попробуй ещё раз или начни с /start.")
    }

    @Throws(Exception::class)
    private fun handleCommand(update: Update, ctx: UpdateContext) {
        val command = CommandUtils.extractCommand(update.message)
        if (command == null) {
            sender.safeReply(ctx, Messages.UNKNOWN_COMMAND)
            return
        }

        for (handler in commandHandlers) {
            if (handler.canHandle(update)) {
                handler.handle(update, ctx)
                return
            }
        }

        sender.safeReply(ctx, Messages.UNKNOWN_COMMAND)
    }

    @Throws(Exception::class)
    private fun handleMessage(update: Update, ctx: UpdateContext) {
        for (handler in messageHandlers) {
            if (handler.canHandle(update, ctx)) {
                handler.handle(update, ctx)
                return
            }
        }

        val isCmd = update.hasMessage() && CommandUtils.isCommand(update.message)
        val fallbackMessage = if (isCmd) Messages.UNKNOWN_COMMAND else Messages.UNKNOWN_MESSAGE
        sender.safeReply(ctx, fallbackMessage)
    }
}
