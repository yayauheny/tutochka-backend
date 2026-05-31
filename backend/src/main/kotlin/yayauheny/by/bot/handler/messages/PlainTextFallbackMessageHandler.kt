package yayauheny.by.bot.handler.messages

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.MessageHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.util.CommandUtils

@Component
@Order(2)
class PlainTextFallbackMessageHandler(
    private val sender: MessageSender,
    private val replyKeyboard: ReplyKeyboardFactory
) : MessageHandler {

    override fun canHandle(update: Update, ctx: UpdateContext): Boolean {
        return ctx.text != null && 
               update.hasMessage() && 
               !CommandUtils.isCommand(update.message)
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        val message = "Не понял запрос. Поделись геолокацией или набери /help"
        sender.sendText(ctx.chatId, message, replyKeyboard.shareLocation())
    }
}
