package yayauheny.by.bot

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.meta.generics.TelegramClient
import yayauheny.by.bot.handler.UpdateContext

@Component
@ConditionalOnProperty(prefix = "telegram.bot", name = ["enabled"], havingValue = "true", matchIfMissing = true)
class TelegramMessageSender(
    private val telegramClient: TelegramClient
) : MessageSender {

    override fun sendMessage(message: SendMessage) {
        try {
            telegramClient.execute(message)
        } catch (e: TelegramApiException) {
            throw IllegalStateException("Failed to send message to chat ${message.chatId}", e)
        }
    }

    override fun editMessage(message: EditMessageText) {
        try {
            telegramClient.execute(message)
        } catch (e: TelegramApiException) {
            throw IllegalStateException("Failed to edit message ${message.messageId}", e)
        }
    }

    override fun sendText(chatId: Long, text: String) {
        sendTextInternal(chatId, text, null, null)
    }

    override fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup) {
        sendTextInternal(chatId, text, replyKeyboard, null)
    }

    override fun sendText(chatId: Long, text: String, inlineKeyboard: InlineKeyboardMarkup) {
        sendTextInternal(chatId, text, null, inlineKeyboard)
    }

    override fun sendText(chatId: Long, text: String, replyKeyboard: ReplyKeyboardMarkup, inlineKeyboard: InlineKeyboardMarkup) {
        sendTextInternal(chatId, text, replyKeyboard, inlineKeyboard)
    }

    private fun sendTextInternal(
        chatId: Long,
        text: String,
        replyKeyboard: ReplyKeyboardMarkup?,
        inlineKeyboard: InlineKeyboardMarkup?
    ) {
        val message = SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .parseMode("HTML")
            .build()

        when {
            replyKeyboard != null -> message.replyMarkup = replyKeyboard
            inlineKeyboard != null -> message.replyMarkup = inlineKeyboard
        }

        sendMessage(message)
    }

    override fun editOrReply(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) {
        if (ctx.isCallback) {
            editMessage(ctx, text, inlineKeyboard)
        } else {
            if (inlineKeyboard == null) {
                sendText(ctx.chatId, text)
            } else {
                sendText(ctx.chatId, text, inlineKeyboard)
            }
        }
    }

    override fun editMessage(ctx: UpdateContext, text: String, inlineKeyboard: InlineKeyboardMarkup?) {
        val messageId = ctx.messageId ?: run {
            if (inlineKeyboard == null) {
                sendText(ctx.chatId, text)
            } else {
                sendText(ctx.chatId, text, inlineKeyboard)
            }
            return
        }

        try {
            val editMessage = EditMessageText.builder()
                .chatId(ctx.chatId.toString())
                .messageId(messageId)
                .text(text)
                .parseMode("HTML")
                .build()

            if (inlineKeyboard != null) {
                editMessage.replyMarkup = inlineKeyboard
            }

            editMessage(editMessage)
        } catch (e: IllegalStateException) {
            if (inlineKeyboard == null) {
                sendText(ctx.chatId, text)
            } else {
                sendText(ctx.chatId, text, inlineKeyboard)
            }
        }
    }

    override fun safeReply(ctx: UpdateContext, errorMessage: String) {
        sendText(ctx.chatId, errorMessage)
    }

    override fun answerCallbackQuery(callbackQueryId: String, text: String?) {
        try {
            val safeText = text?.take(200)
            val answer = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackQueryId)
                .text(safeText)
                .showAlert(false)
                .build()
            telegramClient.execute(answer)
        } catch (e: TelegramApiException) {
            throw IllegalStateException("Failed to answer callback $callbackQueryId", e)
        }
    }
}
