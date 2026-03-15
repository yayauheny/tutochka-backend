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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Order(3)
public class RadiusCallback implements CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(RadiusCallback.class);
    private final MessageSender sender;
    private final UserService userService;
    private final SearchService searchService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public RadiusCallback(MessageSender sender, UserService userService,
                         SearchService searchService,
                         FormatterService formatterService,
                         InlineKeyboardFactory inlineKeyboard,
                         ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.userService = userService;
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

            var sessionOpt = userService.getSession(ctx.userId());
            if (sessionOpt.isEmpty() || sessionOpt.get().location() == null) {
                logger.debug("No session location for userId={}, requesting location", ctx.userId());
                sender.editOrReply(ctx, Messages.LOCATION_NOT_FOUND, null);
                sender.sendText(ctx.chatId(), Messages.LOCATION_REQUEST, replyKeyboard.shareLocation());
                return;
            }

            var location = sessionOpt.get().location();
            logger.debug("Using session location for search: userId={}, lat={}, lon={}, radius={}",
                ctx.userId(), location.latitude(), location.longitude(), radius);

            var results = searchService.findNearby(location.latitude(), location.longitude(), radius, SearchService.DEFAULT_NEAREST_LIMIT);

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
