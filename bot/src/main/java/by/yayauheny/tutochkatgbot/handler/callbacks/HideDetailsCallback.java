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
import java.util.UUID;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for "hide details" callback (hide:<id>)
 */
@Component
@Order(3)
public class HideDetailsCallback implements CallbackHandler {
    private static final Logger logger = LoggerFactory.getLogger(HideDetailsCallback.class);
    private final MessageSender sender;
    private final SearchService searchService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;

    public HideDetailsCallback(MessageSender sender, SearchService searchService,
                              FormatterService formatterService, InlineKeyboardFactory inlineKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
    }

    @Override
    public String prefix() {
        return "hide";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "hide");
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
            
            Double distanceMeters = searchService.getDetailDistanceMeters(UUID.fromString(toiletId))
                    .orElse(toilet.distanceMeters() != null ? toilet.distanceMeters().doubleValue() : null);
            
            String text = formatterService.toiletDetailsCompact(toilet, distanceMeters);
            sender.editOrReply(ctx, text, inlineKeyboard.toiletDetailsCompact(toilet));
        } catch (IllegalArgumentException e) {
            logger.warn("Toilet not found: {}", e.getMessage());
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
        } catch (Exception e) {
            logger.error("Error handling hide details callback: {}", e.getMessage(), e);
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
        }
    }
}

