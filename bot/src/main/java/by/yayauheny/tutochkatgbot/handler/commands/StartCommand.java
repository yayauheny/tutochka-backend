package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.CommandHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.CommandUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for /start command
 */
@Component
@Order(1)
public class StartCommand implements CommandHandler {
    private final MessageSender sender;
    private final ReplyKeyboardFactory replyKeyboard;

    public StartCommand(MessageSender sender, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        String command = CommandUtils.extractCommand(message);
        return "/start".equals(command);
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        sender.sendText(ctx.chatId(), Messages.WELCOME_MESSAGE, replyKeyboard.shareLocation());
    }
}
