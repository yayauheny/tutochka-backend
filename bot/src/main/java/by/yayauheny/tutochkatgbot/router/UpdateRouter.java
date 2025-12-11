package by.yayauheny.tutochkatgbot.router;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.*;
import by.yayauheny.tutochkatgbot.messages.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

/**
 * Router for handling different types of updates
 */
@Component
public class UpdateRouter {
    private static final Logger logger = LoggerFactory.getLogger(UpdateRouter.class);
    
    private final List<CommandHandler> commandHandlers;
    private final List<CallbackHandler> callbackHandlers;
    private final List<MessageHandler> messageHandlers;
    private final MessageSender sender;
    
    public UpdateRouter(List<CommandHandler> commandHandlers,
                       List<CallbackHandler> callbackHandlers,
                       List<MessageHandler> messageHandlers,
                       MessageSender sender) {
        this.commandHandlers = commandHandlers;
        this.callbackHandlers = callbackHandlers;
        this.messageHandlers = messageHandlers;
        this.sender = sender;
    }
    
    /**
     * Route update to appropriate handler
     * @param update Telegram update
     */
    public void route(Update update) {
        UpdateContext ctx = UpdateContext.from(update);
        
        logger.debug("Routing update: chatId={}, userId={}, hasMessage={}, hasCallback={}, hasLocation={}, locationSource={}", 
            ctx.chatId(), ctx.userId(), update.hasMessage(), update.hasCallbackQuery(), 
            ctx.hasLocation(), ctx.hasLocation() ? (update.hasMessage() && update.getMessage().hasLocation() ? "location" : "venue") : "none");
        
        if (update.hasCallbackQuery()) {
            handleCallback(update, ctx);
            return;
        }
        
        if (ctx.text() != null && ctx.text().startsWith("/")) {
            handleCommand(update, ctx);
            return;
        }
        
        handleMessage(update, ctx);
    }
    
    private void handleCallback(Update update, UpdateContext ctx) {
        String callbackData = ctx.callbackData();
        if (callbackData == null) {
            logger.warn("Callback query without data: {}", update.getCallbackQuery());
            return;
        }
        
        for (CallbackHandler handler : callbackHandlers) {
            if (handler.canHandle(callbackData)) {
                try {
                    logger.debug("Handling callback with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, ctx);
                    return;
                } catch (Exception e) {
                    logger.error("Error in callback handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    sender.safeReply(ctx, Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No callback handler found for data: {}", callbackData);
        sender.safeReply(ctx, "Действие недоступно. Попробуй ещё раз или начни с /start.");
    }
    
    private void handleCommand(Update update, UpdateContext ctx) {
        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(update)) {
                try {
                    logger.debug("Handling command with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, ctx);
                    return;
                } catch (Exception e) {
                    logger.error("Error in command handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    sender.safeReply(ctx, Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No command handler found for: {}", ctx.text());
        sender.safeReply(ctx, Messages.UNKNOWN_COMMAND);
    }
    
    private void handleMessage(Update update, UpdateContext ctx) {
        for (MessageHandler handler : messageHandlers) {
            if (handler.canHandle(update, ctx)) {
                try {
                    logger.debug("Handling message with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, ctx);
                    return;
                } catch (Exception e) {
                    logger.error("Error in message handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    sender.safeReply(ctx, Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No message handler found for update: {}", update);
        sender.safeReply(ctx, Messages.UNKNOWN_COMMAND);
    }
}
