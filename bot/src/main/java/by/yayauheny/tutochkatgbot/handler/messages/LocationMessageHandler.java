package by.yayauheny.tutochkatgbot.handler.messages;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.MessageHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for location messages
 */
@Component
public class LocationMessageHandler implements MessageHandler {
    private final MessageSender sender;
    private final SearchService searchService;
    private final UserService userService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public LocationMessageHandler(MessageSender sender, SearchService searchService, UserService userService,
                                 FormatterService formatterService, InlineKeyboardFactory inlineKeyboard, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.userService = userService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public boolean canHandle(Update update, UpdateContext ctx) {
        return ctx.hasLocation();
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        // Save user location
        userService.saveLocation(ctx.userId(), ctx.latitude(), ctx.longitude());
        
        // Get user radius preference
        int radius = userService.getRadius(ctx.userId()).orElse(UserService.DEFAULT_RADIUS);
        
        // Search for nearby toilets
        var results = searchService.findNearby(ctx.latitude(), ctx.longitude(), radius, 10);
        
        if (results.isEmpty()) {
            // Send both inline keyboard for radius selection and reply keyboard for location sharing
            sender.sendText(ctx.chatId(), Messages.NO_TOILETS_FOUND, inlineKeyboard.radiusSelection());
            sender.sendText(ctx.chatId(), "Или попробуйте другую точку:", replyKeyboard.shareLocation());
            return;
        }
        
        String message = formatterService.toiletsFound(results.size());
        sender.sendText(ctx.chatId(), message, inlineKeyboard.toiletList(results));
    }
}
