package yayauheny.by.bot

import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import yayauheny.by.bot.handler.UpdateContext

interface MessageSender {
    @Throws(TelegramApiException::class)
    fun sendMessage(message: SendMessage)

    @Throws(TelegramApiException::class)
    fun editMessage(message: EditMessageText)

    fun sendText(chatId: Long, text: String)
    fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup)
    fun sendText(chatId: Long, text: String, inlineKeyboard: InlineKeyboardMarkup)
    fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup, inlineKeyboard: InlineKeyboardMarkup)
    fun editOrReply(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?)
    fun editMessage(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?)
    fun safeReply(ctx: UpdateContext, errorMessage: String)

    fun answerCallbackQuery(callbackQueryId: String, text: String?)
}
