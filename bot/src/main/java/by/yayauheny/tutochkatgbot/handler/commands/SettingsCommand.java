package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.CommandHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.service.UserService;
import by.yayauheny.tutochkatgbot.util.CommandUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for /settings command
 */
@Component
@Order(3)
public class SettingsCommand implements CommandHandler {
    private final MessageSender sender;
    private final UserService userService;
    private final InlineKeyboardFactory inlineKeyboard;

    public SettingsCommand(MessageSender sender, UserService userService, InlineKeyboardFactory inlineKeyboard) {
        this.sender = sender;
        this.userService = userService;
        this.inlineKeyboard = inlineKeyboard;
    }

    @Override
    public String command() {
        return "/settings";
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        String command = CommandUtils.extractCommand(message);
        return "/settings".equals(command);
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        String message = "🔧 Настройки:\n\n" +
                "• Радиус поиска: " + userService.getRadius(ctx.userId()).orElse(UserService.DEFAULT_RADIUS) + " м\n" +
                "• Единицы измерения: метры";
        
        sender.sendText(ctx.chatId(), message, inlineKeyboard.radiusSelection());
    }
}
