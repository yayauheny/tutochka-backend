package by.yayauheny.tutochkatgbot.handler.messages;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.MessageHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Fallback handler for unknown text messages
 */
@Component
public class PlainTextFallbackMessageHandler implements MessageHandler {
    private final MessageSender sender;
    private final ReplyKeyboardFactory replyKeyboard;

    public PlainTextFallbackMessageHandler(MessageSender sender, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public boolean canHandle(Update update, UpdateContext ctx) {
        // Handle text messages that don't match any commands
        return ctx.text() != null && !ctx.text().startsWith("/");
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        String message = "Не понял запрос. Поделись геолокацией или наберите /help";
        sender.sendText(ctx.chatId(), message, replyKeyboard.shareLocation());
    }
}
