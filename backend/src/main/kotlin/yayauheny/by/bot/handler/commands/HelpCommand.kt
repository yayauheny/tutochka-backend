package yayauheny.by.bot.handler.commands

import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.objects.Update
import yayauheny.by.bot.MessageSender
import yayauheny.by.bot.handler.CommandHandler
import yayauheny.by.bot.handler.UpdateContext
import yayauheny.by.bot.keyboard.ReplyKeyboardFactory
import yayauheny.by.bot.messages.Messages
import yayauheny.by.bot.util.CommandUtils

@Component
@Order(2)
class HelpCommand(
    private val sender: MessageSender,
    private val replyKeyboard: ReplyKeyboardFactory
) : CommandHandler {

    override fun command(): String {
        return "/help"
    }

    override fun canHandle(update: Update): Boolean {
        if (!update.hasMessage()) {
            return false
        }
        val command = CommandUtils.extractCommand(update.message)
        return "/help".equals(command, ignoreCase = true)
    }

    override fun handle(update: Update, ctx: UpdateContext) {
        sender.sendText(ctx.chatId, Messages.HELP_MESSAGE, replyKeyboard.helpAndLocation(ctx.userId, ctx.isPrivateChat))
    }
}
