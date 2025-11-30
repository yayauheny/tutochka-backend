package by.yayauheny.tutochkatgbot.bot;

import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Interface for sending messages
 */
public interface MessageSender {
    void sendMessage(SendMessage message) throws TelegramApiException;
    void editMessage(EditMessageText message) throws TelegramApiException;
    
    // Convenience methods
    void sendText(long chatId, String text);
    void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard);
    void sendText(long chatId, String text, InlineKeyboardMarkup inlineKeyboard);
    void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard, InlineKeyboardMarkup inlineKeyboard);
    void editOrReply(UpdateContext ctx, String text, InlineKeyboardMarkup inlineKeyboard);
    void editMessage(UpdateContext ctx, String text, InlineKeyboardMarkup inlineKeyboard);
    void safeReply(UpdateContext ctx, String errorMessage);
}
