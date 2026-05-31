package yayauheny.by.bot

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import yayauheny.by.bot.handler.UpdateContext

@Component
@ConditionalOnMissingBean(MessageSender::class)
class NoOpMessageSender : MessageSender {
    override fun sendMessage(message: SendMessage) = Unit

    override fun editMessage(message: EditMessageText) = Unit

    override fun sendText(chatId: Long, text: String) = Unit

    override fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup) = Unit

    override fun sendText(chatId: Long, text: String, inlineKeyboard: InlineKeyboardMarkup) = Unit

    override fun sendText(
        chatId: Long,
        text: String,
        replyKeyboard: ReplyKeyboardMarkup,
        inlineKeyboard: InlineKeyboardMarkup
    ) = Unit

    override fun editOrReply(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) = Unit

    override fun editMessage(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) = Unit

    override fun safeReply(ctx: UpdateContext, errorMessage: String) = Unit

    override fun answerCallbackQuery(callbackQueryId: String, text: String?) = Unit
}
