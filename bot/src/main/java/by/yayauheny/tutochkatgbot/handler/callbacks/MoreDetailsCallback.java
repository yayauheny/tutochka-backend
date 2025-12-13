package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for "more details" callback (m:<id>)
 */
@Component
@Order(2)
public class MoreDetailsCallback implements CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(MoreDetailsCallback.class);
    private final MessageSender sender;
    private final SearchService searchService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;

    public MoreDetailsCallback(MessageSender sender, SearchService searchService,
                              FormatterService formatterService, InlineKeyboardFactory inlineKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
    }

    @Override
    public String prefix() {
        return "m";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "m");
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        try {
            String toiletId = CallbackData.arg(ctx.callbackData());
            
            if (toiletId == null || toiletId.isBlank()) {
                logger.warn("Empty toilet ID in callback: {}", ctx.callbackData());
                sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
                return;
            }
            
            var toilet = searchService.getById(toiletId)
                    .orElseThrow(() -> new IllegalArgumentException("Toilet not found: " + toiletId));
            
            // Get distance from toilet DTO if available, otherwise null
            Double distanceMeters = toilet.distanceMeters() != null 
                ? toilet.distanceMeters().doubleValue() 
                : null;
            
            // Show full card
            String text = formatterService.toiletDetailsFull(toilet, distanceMeters);
            sender.editOrReply(ctx, text, inlineKeyboard.toiletDetailsFull(toilet));
        } catch (IllegalArgumentException e) {
            logger.warn("Toilet not found: {}", e.getMessage());
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
        } catch (Exception e) {
            logger.error("Error handling more details callback: {}", e.getMessage(), e);
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
        }
    }
}

