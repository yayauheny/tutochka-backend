package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.LastLocationCacheService;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for radius selection callbacks (radius:<meters>)
 */
@Component
@Order(3)
public class RadiusCallback implements CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(RadiusCallback.class);
    private final MessageSender sender;
    private final UserService userService;
    private final LastLocationCacheService lastLocationCache;
    private final SearchService searchService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public RadiusCallback(MessageSender sender, UserService userService,
                         LastLocationCacheService lastLocationCache,
                         SearchService searchService,
                         FormatterService formatterService,
                         InlineKeyboardFactory inlineKeyboard,
                         ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.userService = userService;
        this.lastLocationCache = lastLocationCache;
        this.searchService = searchService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public String prefix() {
        return "radius";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "radius");
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        try {
            String radiusStr = CallbackData.arg(ctx.callbackData());
            if (radiusStr == null || radiusStr.isBlank()) {
                logger.warn("Empty radius in callback: {}", ctx.callbackData());
                sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
                return;
            }
            
            int radius = Integer.parseInt(radiusStr);
            if (radius <= 0) {
                logger.warn("Invalid radius value: {}", radius);
                sender.editOrReply(ctx, "❌ Ошибка: радиус должен быть больше 0", null);
                return;
            }
            
            userService.setRadius(ctx.userId(), radius);
            
            var lastLocationOpt = lastLocationCache.getLastLocation(ctx.chatId());
            
            if (lastLocationOpt.isEmpty()) {
                logger.debug("No cached location for chatId={}, requesting location", ctx.chatId());
                sender.editOrReply(ctx, Messages.LOCATION_NOT_FOUND, null);
                sender.sendText(ctx.chatId(), Messages.LOCATION_REQUEST, replyKeyboard.shareLocation());
                return;
            }
            
            var lastLocation = lastLocationOpt.get();
            logger.debug("Using cached location for search: chatId={}, lat={}, lon={}, radius={}", 
                ctx.chatId(), lastLocation.latitude(), lastLocation.longitude(), radius);
            
            var results = searchService.findNearby(lastLocation.latitude(), lastLocation.longitude(), radius, 10);
            
            if (results.isEmpty()) {
                sender.editOrReply(ctx, Messages.NO_TOILETS_FOUND, inlineKeyboard.radiusSelection());
                return;
            }
            
            String message = formatterService.toiletsFound(results.size());
            sender.editOrReply(ctx, message, inlineKeyboard.toiletList(results));
        } catch (NumberFormatException e) {
            logger.warn("Invalid radius format in callback: {}", ctx.callbackData(), e);
            sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
        }
    }
}
