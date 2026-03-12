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
 * Handler for /help command
 */
@Component
@Order(2)
public class HelpCommand implements CommandHandler {
    private final MessageSender sender;
    private final ReplyKeyboardFactory replyKeyboard;

    public HelpCommand(MessageSender sender, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public String command() {
        return "/help";
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        String command = CommandUtils.extractCommand(message);
        return "/help".equalsIgnoreCase(command);
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        sender.sendText(ctx.chatId(), Messages.HELP_MESSAGE, replyKeyboard.helpAndLocation());
    }
}
