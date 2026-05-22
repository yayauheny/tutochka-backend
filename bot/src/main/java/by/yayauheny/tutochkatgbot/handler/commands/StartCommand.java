package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.CommandHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.CommandUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import yayauheny.by.contract.BackendClient;
import yayauheny.by.model.analytics.AnalyticsEventRequest;
import yayauheny.by.model.analytics.ProductAnalyticsEvent;

import java.util.Map;

import kotlinx.serialization.json.JsonObject;

/**
 * Handler for /start command
 */
@Component
@Order(1)
public class StartCommand implements CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(StartCommand.class);
    private static final String DIRECT_TAG = "direct";
    private static final String WEBSITE_TAG = "website";
    private static final String OTHER_TAG = "other";

    private final MessageSender sender;
    private final ReplyKeyboardFactory replyKeyboard;
    private final BackendClient backendClient;

    public StartCommand(MessageSender sender, ReplyKeyboardFactory replyKeyboard, BackendClient backendClient) {
        this.sender = sender;
        this.replyKeyboard = replyKeyboard;
        this.backendClient = backendClient;
    }

    @Override
    public String command() {
        return "/start";
    }

    @Override
    public boolean canHandle(Update update) {
        if (!update.hasMessage()) {
            return false;
        }
        Message message = update.getMessage();
        String command = CommandUtils.extractCommand(message);
        return "/start".equals(command);
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        trackBotStarted(update, ctx);
        sender.sendText(ctx.chatId(), Messages.WELCOME_MESSAGE, replyKeyboard.shareLocation());
    }

    private void trackBotStarted(Update update, UpdateContext ctx) {
        try {
            String acquisitionTag = resolveAcquisitionTag(update.getMessage());
            AnalyticsEventRequest request =
                new AnalyticsEventRequest(
                    ProductAnalyticsEvent.BOT_STARTED,
                    null,
                    null,
                    null,
                    null,
                    acquisitionTag,
                    new JsonObject(Map.of())
                );
            backendClient.trackProductEvent(request, ctx.userId(), ctx.chatId(), ctx.username());
        } catch (Exception exception) {
            log.warn("Failed to track bot start analytics", exception);
        }
    }

    private String resolveAcquisitionTag(Message message) {
        String payload = CommandUtils.extractCommandArgument(message);
        if (payload == null) {
            return DIRECT_TAG;
        }

        return switch (payload.trim().toLowerCase()) {
            case WEBSITE_TAG -> WEBSITE_TAG;
            case DIRECT_TAG -> DIRECT_TAG;
            default -> OTHER_TAG;
        };
    }
}
