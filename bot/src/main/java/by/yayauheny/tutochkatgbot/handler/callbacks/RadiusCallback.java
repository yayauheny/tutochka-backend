package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.service.UserService;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for radius selection callbacks (r:<meters>)
 */
@Component
public class RadiusCallback implements CallbackHandler {
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
            int radius = Integer.parseInt(CallbackData.arg(ctx.callbackData()));
            userService.setRadius(ctx.userId(), radius);
            
            String message = "✅ Радиус поиска изменен на " + radius + " м";
            sender.editOrReply(ctx, message, null);
        } catch (NumberFormatException e) {
            sender.editOrReply(ctx, "❌ Ошибка: неверный радиус", null);
        }
    }
}
