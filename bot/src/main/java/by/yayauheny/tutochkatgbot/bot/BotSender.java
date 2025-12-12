package by.yayauheny.tutochkatgbot.bot;

import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * Service for sending messages and handling errors
 */
@Component
public class BotSender implements MessageSender {
    private static final Logger logger = LoggerFactory.getLogger(BotSender.class);
    private final TelegramWebhookBot bot;

    public BotSender(TelegramWebhookBot bot) {
        this.bot = bot;
    }

    /**
     * Send text message
     * @param chatId chat ID
     * @param text message text
     */
    public void sendText(long chatId, String text) {
        sendText(chatId, text, null, null);
    }

    /**
     * Send text message with reply keyboard
     * @param chatId chat ID
     * @param text message text
     * @param replyKeyboard reply keyboard
     */
    public void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard) {
        sendText(chatId, text, replyKeyboard, null);
    }

    /**
     * Send text message with inline keyboard
     * @param chatId chat ID
     * @param text message text
     * @param inlineKeyboard inline keyboard
     */
    public void sendText(long chatId, String text, InlineKeyboardMarkup inlineKeyboard) {
        sendText(chatId, text, null, inlineKeyboard);
    }

    /**
     * Send text message with keyboards
     * @param chatId chat ID
     * @param text message text
     * @param replyKeyboard reply keyboard
     * @param inlineKeyboard inline keyboard
     */
    public void sendText(long chatId, String text, ReplyKeyboardMarkup replyKeyboard, InlineKeyboardMarkup inlineKeyboard) {
        try {
            SendMessage message = SendMessage.builder()
                    .chatId(String.valueOf(chatId))
                    .text(text)
                    .build();
            
            if (replyKeyboard != null) {
                message.setReplyMarkup(replyKeyboard);
            } else if (inlineKeyboard != null) {
                message.setReplyMarkup(inlineKeyboard);
            }
            
            bot.execute(message);
            logger.debug("Sent message to chat {}: {}", chatId, text);
        } catch (TelegramApiException e) {
            logger.error("Failed to send message to chat {}: {}", chatId, e.getMessage(), e);
        }
    }

    /**
     * Edit or reply to message
     * @param ctx update context
     * @param text new text
     * @param inlineKeyboard inline keyboard
     */
    public void editOrReply(UpdateContext ctx, String text, InlineKeyboardMarkup inlineKeyboard) {
        if (ctx.isCallback()) {
            editMessage(ctx, text, inlineKeyboard);
        } else {
            sendText(ctx.chatId(), text, inlineKeyboard);
        }
    }

    /**
     * Edit message
     * @param ctx update context
     * @param text new text
     * @param inlineKeyboard inline keyboard
     */
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
                .build();
            
            if (inlineKeyboard != null) {
                editMessage.setReplyMarkup(inlineKeyboard);
            }
            
            bot.execute(editMessage);
            logger.debug("Edited message in chat {}: {}", ctx.chatId(), text);
        } catch (TelegramApiException e) {
            logger.error("Failed to edit message in chat {}: {}", ctx.chatId(), e.getMessage(), e);
            sendText(ctx.chatId(), text, inlineKeyboard);
        }
    }

    /**
     * Send error message safely
     * @param ctx update context
     * @param errorMessage error message
     */
    public void safeReply(UpdateContext ctx, String errorMessage) {
        // sendText already handles exceptions internally, so no need for try-catch here
        sendText(ctx.chatId(), errorMessage);
    }

    @Override
    public void sendMessage(SendMessage message) throws TelegramApiException {
        bot.execute(message);
    }

    @Override
    public void editMessage(EditMessageText message) throws TelegramApiException {
        bot.execute(message);
    }
    
    @Override
    public void answerCallbackQuery(String callbackQueryId, String text) {
        try {
            AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                    .callbackQueryId(callbackQueryId)
                    .text(text)
                    .showAlert(text != null && text.length() > 200) // Show alert for long messages
                    .build();
            bot.execute(answer);
            logger.debug("Answered callback query: {}", callbackQueryId);
        } catch (TelegramApiException e) {
            logger.error("Failed to answer callback query {}: {}", callbackQueryId, e.getMessage(), e);
        }
    }
}
