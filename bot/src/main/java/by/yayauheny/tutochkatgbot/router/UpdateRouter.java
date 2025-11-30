package by.yayauheny.tutochkatgbot.router;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.*;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.*;
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
    private final UserService userService;
    private final SearchService searchService;
    private final FormatterService formatterService;
    
    public UpdateRouter(List<CommandHandler> commandHandlers,
                       List<CallbackHandler> callbackHandlers,
                       List<MessageHandler> messageHandlers,
                       MessageSender sender,
                       UserService userService,
                       SearchService searchService,
                       FormatterService formatterService) {
        this.commandHandlers = commandHandlers;
        this.callbackHandlers = callbackHandlers;
        this.messageHandlers = messageHandlers;
        this.sender = sender;
        this.userService = userService;
        this.searchService = searchService;
        this.formatterService = formatterService;
    }
    
    /**
     * Route update to appropriate handler
     * @param update Telegram update
     */
    public void route(Update update) {
        UpdateContext ctx = UpdateContext.from(update);
        BotContext botContext = new BotContext(ctx, sender, userService, searchService, formatterService);
        
        logger.debug("Routing update: chatId={}, userId={}, hasMessage={}, hasCallback={}, hasLocation={}, locationSource={}", 
            ctx.chatId(), ctx.userId(), update.hasMessage(), update.hasCallbackQuery(), 
            ctx.hasLocation(), ctx.hasLocation() ? (update.hasMessage() && update.getMessage().hasLocation() ? "location" : "venue") : "none");
        
        // Handle callback queries first
        if (update.hasCallbackQuery()) {
            handleCallback(update, botContext);
            return;
        }
        
        // Handle commands
        if (ctx.text() != null && ctx.text().startsWith("/")) {
            handleCommand(update, botContext);
            return;
        }
        
        // Handle other messages
        handleMessage(update, botContext);
    }
    
    private void handleCallback(Update update, BotContext botContext) {
        String callbackData = botContext.ctx().callbackData();
        if (callbackData == null) {
            logger.warn("Callback query without data: {}", update.getCallbackQuery());
            return;
        }
        
        for (CallbackHandler handler : callbackHandlers) {
            if (handler.canHandle(callbackData)) {
                try {
                    logger.debug("Handling callback with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, botContext.ctx());
                    return;
                } catch (Exception e) {
                    logger.error("Error in callback handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No callback handler found for data: {}", callbackData);
        botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
    }
    
    private void handleCommand(Update update, BotContext botContext) {
        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(update)) {
                try {
                    logger.debug("Handling command with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, botContext.ctx());
                    return;
                } catch (Exception e) {
                    logger.error("Error in command handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No command handler found for: {}", botContext.ctx().text());
        botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
    }
    
    private void handleMessage(Update update, BotContext botContext) {
        for (MessageHandler handler : messageHandlers) {
            if (handler.canHandle(update, botContext.ctx())) {
                try {
                    logger.debug("Handling message with handler: {}", handler.getClass().getSimpleName());
                    handler.handle(update, botContext.ctx());
                    return;
                } catch (Exception e) {
                    logger.error("Error in message handler {}: {}", handler.getClass().getSimpleName(), e.getMessage(), e);
                    botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
                    return;
                }
            }
        }
        
        logger.warn("No message handler found for update: {}", update);
        botContext.out().safeReply(botContext.ctx(), Messages.SOMETHING_WENT_WRONG);
    }
}
