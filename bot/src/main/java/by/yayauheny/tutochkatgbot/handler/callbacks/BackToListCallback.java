package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
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
 * Handler for back to list callback (b:list)
 */
@Component
public class BackToListCallback implements CallbackHandler {
    private final MessageSender sender;
    private final SearchService searchService;
    private final UserService userService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public BackToListCallback(MessageSender sender, SearchService searchService, UserService userService,
                             FormatterService formatterService, InlineKeyboardFactory inlineKeyboard, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.userService = userService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public String prefix() {
        return "b";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "b") && "list".equals(CallbackData.arg(callbackData));
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        // Get user session to retrieve last location
        var session = userService.getSession(ctx.userId());
        
        if (session.isEmpty() || session.get().location() == null) {
            sender.sendText(ctx.chatId(), Messages.LOCATION_REQUEST, replyKeyboard.shareLocation());
            return;
        }
        
        var location = session.get().location();
        int radius = userService.getRadius(ctx.userId()).orElse(500);
        
        // Search for nearby toilets again
        var results = searchService.findNearby(location.latitude(), location.longitude(), radius, 10);
        
        if (results.isEmpty()) {
            sender.sendText(ctx.chatId(), Messages.NO_TOILETS_FOUND, replyKeyboard.shareLocation());
            return;
        }
        
        String message = formatterService.toiletsFound(results.size());
        sender.editOrReply(ctx, message, inlineKeyboard.toiletList(results));
    }
}
