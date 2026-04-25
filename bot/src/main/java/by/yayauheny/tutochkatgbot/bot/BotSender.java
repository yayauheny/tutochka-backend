package by.yayauheny.tutochkatgbot.bot;

import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class BotSender implements MessageSender {
    private final TelegramClient telegramClient;

    public BotSender(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void sendText(long chatId, String text) {
        sendText(chatId, text, null, null);
    }

    public void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard) {
        sendText(chatId, text, replyKeyboard, null);
    }

    public void sendText(long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        sendText(chatId, text, null, inlineKeyboard);
    }

    public void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard, InlineKeyboardMarkup inlineKeyboard) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .parseMode("HTML")
                    .build();
            
            if (replyKeyboard != null) {
                message.setReplyMarkup(replyKeyboard);
            } else if (inlineKeyboard != null) {
                message.setReplyMarkup(inlineKeyboard);
            }
            
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to send message to chat " + chatId, e);
        }
    }

    public void editOrReply(UpdateContext ctx, String text, InlineKeyboardMarkup inlineKeyboard) {
        if (ctx.isCallback()) {
            editMessage(ctx, text, inlineKeyboard);
        } else {
            sendText(ctx.chatId(), text, inlineKeyboard);
        }
    }

    public void editMessage(UpdateContext ctx, String text, InlineKeyboardMarkup inlineKeyboard) {
        if (ctx.messageId() == null) {
            sendText(ctx.chatId(), text, inlineKeyboard);
            return;
        }
        
        try {
            EditMessageText editMessage = EditMessageText.builder()
                .chatId(String.valueOf(ctx.chatId()))
                .messageId(ctx.messageId())
                .text(text)
                .parseMode("HTML")
                .build();
            
            if (inlineKeyboard != null) {
                editMessage.setReplyMarkup(inlineKeyboard);
            }
            
            telegramClient.execute(editMessage);
        } catch (TelegramApiException e) {
            sendText(ctx.chatId(), text, inlineKeyboard);
        }
    }

    public void safeReply(UpdateContext ctx, String errorMessage) {
        sendText(ctx.chatId(), errorMessage);
    }

    @Override
    public void sendMessage(SendMessage message) throws TelegramApiException {
        telegramClient.execute(message);
    }

    @Override
    public void editMessage(EditMessageText message) throws TelegramApiException {
        telegramClient.execute(message);
    }
    
    @Override
    public void answerCallbackQuery(String callbackQueryId, String text) {
        try {
            String safeText = text;
            if (safeText != null && safeText.length() > 200) {
                safeText = safeText.substring(0, 200);
            }
            
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(safeText)
                    .showAlert(false)
                    .build();
            telegramClient.execute(answer);
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Failed to answer callback " + callbackQueryId, e);
        }
    }
}
