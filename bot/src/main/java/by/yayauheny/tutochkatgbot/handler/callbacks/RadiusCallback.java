package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
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

@Component
@Order(3)
public class RadiusCallback implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(RadiusCallback.class);

    private final MessageSender sender;
    private final UserService userService;
    private final SearchService searchService;
    private final BackListSnapshotCache backListSnapshotCache;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public RadiusCallback(MessageSender sender, UserService userService,
                         SearchService searchService,
                         BackListSnapshotCache backListSnapshotCache,
                         FormatterService formatterService,
                         InlineKeyboardFactory inlineKeyboard,
                         ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.userService = userService;
        this.searchService = searchService;
        this.backListSnapshotCache = backListSnapshotCache;
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
            if (radiusStr.isBlank()) {
                sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, outcome=invalid_radius",
                    ctx.chatId(),
                    ctx.userId()
                );
                return;
            }

            int radius = Integer.parseInt(radiusStr);
            if (radius <= 0) {
                sender.editOrReply(ctx, "❌ Ошибка: радиус должен быть больше 0", null);
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, outcome=invalid_radius",
                    ctx.chatId(),
                    ctx.userId(),
                    radius
                );
                return;
            }

            var sessionOpt = userService.getSession(ctx.userId());
            if (sessionOpt.isEmpty() || sessionOpt.get().location() == null) {
                sender.editOrReply(ctx, Messages.LOCATION_NOT_FOUND, null);
                sender.sendText(ctx.chatId(), Messages.LOCATION_REQUEST, replyKeyboard.shareLocation());
                log.warn(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, outcome=missing_location",
                    ctx.chatId(),
                    ctx.userId(),
                    radius
                );
                return;
            }

            var location = sessionOpt.get().location();
            var results = searchService.findNearby(location.latitude(), location.longitude(), radius, SearchService.DEFAULT_NEAREST_LIMIT);

            if (results.isEmpty()) {
                sender.editOrReply(ctx, Messages.NO_TOILETS_FOUND, inlineKeyboard.radiusSelection());
                log.info(
                    "Handled radius callback: chatId={}, userId={}, radiusMeters={}, resultCount=0, outcome=no_results",
                    ctx.chatId(),
                    ctx.userId(),
                    radius
                );
                return;
            }

            String message = formatterService.toiletsFound(results.size());
            sender.editOrReply(ctx, message, inlineKeyboard.toiletList(results));
            backListSnapshotCache.store(ctx.chatId(), ctx.userId(), radius, results);
            log.info(
                "Handled radius callback: chatId={}, userId={}, radiusMeters={}, resultCount={}, outcome=results_sent",
                ctx.chatId(),
                ctx.userId(),
                radius,
                results.size()
            );
        } catch (NumberFormatException e) {
            sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
            log.warn(
                "Handled radius callback: chatId={}, userId={}, outcome=invalid_radius",
                ctx.chatId(),
                ctx.userId()
            );
        }
    }
}
