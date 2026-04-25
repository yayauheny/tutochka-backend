package by.yayauheny.tutochkatgbot.handler.messages;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.MessageHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for location messages
 */
@Component
@Order(1)
public class LocationMessageHandler implements MessageHandler {
    private static final Logger log = LoggerFactory.getLogger(LocationMessageHandler.class);

    private final MessageSender sender;
    private final SearchService searchService;
    private final UserService userService;
    private final BackListSnapshotCache backListSnapshotCache;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public LocationMessageHandler(MessageSender sender, SearchService searchService, UserService userService,
                                 BackListSnapshotCache backListSnapshotCache,
                                 FormatterService formatterService, InlineKeyboardFactory inlineKeyboard, ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.userService = userService;
        this.backListSnapshotCache = backListSnapshotCache;
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
        Double latitude = ctx.latitude();
        Double longitude = ctx.longitude();
        
        if (latitude == null || longitude == null) {
            sender.sendText(ctx.chatId(), Messages.SOMETHING_WENT_WRONG);
            log.warn("Handled location message: chatId={}, userId={}, outcome=missing_location", ctx.chatId(), ctx.userId());
            return;
        }
        
        userService.saveLocation(ctx.userId(), latitude, longitude);

        var results = searchService.findNearby(latitude, longitude, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);
        
        if (results.isEmpty()) {
            sender.sendText(ctx.chatId(), Messages.NO_TOILETS_FOUND, inlineKeyboard.radiusSelection());
            sender.sendText(ctx.chatId(), "Или попробуйте другую точку:", replyKeyboard.shareLocation());
            log.info(
                "Handled location search: chatId={}, userId={}, radiusMeters={}, resultCount=0, outcome=no_results",
                ctx.chatId(),
                ctx.userId(),
                UserService.DEFAULT_RADIUS
            );
            return;
        }

        String message = formatterService.toiletsFound(results.size());
        sender.sendText(ctx.chatId(), message, inlineKeyboard.toiletList(results));
        backListSnapshotCache.store(ctx.chatId(), ctx.userId(), UserService.DEFAULT_RADIUS, results);
        log.info(
            "Handled location search: chatId={}, userId={}, radiusMeters={}, resultCount={}, outcome=results_sent",
            ctx.chatId(),
            ctx.userId(),
            UserService.DEFAULT_RADIUS,
            results.size()
        );
    }
}
