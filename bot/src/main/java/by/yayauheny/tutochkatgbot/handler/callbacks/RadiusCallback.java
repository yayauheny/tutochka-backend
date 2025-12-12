package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for radius selection callbacks (r:<meters>)
 */
@Component
@Order(3)
public class RadiusCallback implements CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(RadiusCallback.class);
    private final MessageSender sender;
    private final UserService userService;

    public RadiusCallback(MessageSender sender, UserService userService) {
        this.sender = sender;
        this.userService = userService;
    }

    @Override
    public String prefix() {
        return "r";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "r");
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
            
            String message = "✅ Радиус поиска изменен на " + radius + " м";
            sender.editOrReply(ctx, message, null);
        } catch (NumberFormatException e) {
            logger.warn("Invalid radius format in callback: {}", ctx.callbackData(), e);
            sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
        }
    }
}
