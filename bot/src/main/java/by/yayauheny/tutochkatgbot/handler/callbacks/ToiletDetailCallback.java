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
 * Handler for toilet detail callbacks (detail:<id>)
 */
@Component
@Order(1)
public class ToiletDetailCallback implements CallbackHandler {
    private static final Logger log = LoggerFactory.getLogger(ToiletDetailCallback.class);

    private final MessageSender sender;
    private final SearchService searchService;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;

    public ToiletDetailCallback(MessageSender sender, SearchService searchService,
                               FormatterService formatterService, InlineKeyboardFactory inlineKeyboard) {
        this.sender = sender;
        this.searchService = searchService;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
    }

    @Override
    public String prefix() {
        return "detail";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "detail");
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        String toiletId = CallbackData.arg(ctx.callbackData());

        if (toiletId == null || toiletId.isBlank()) {
            sender.editOrReply(ctx, Messages.SOMETHING_WENT_WRONG, null);
            log.warn(
                "Handled detail callback: chatId={}, userId={}, outcome=invalid_id",
                ctx.chatId(),
                ctx.userId()
            );
            return;
        }

        var toilet = searchService.getById(toiletId)
                .orElseThrow(() -> new IllegalArgumentException("Toilet not found: " + toiletId));

        Double distanceMeters = toilet.getDistanceMeters() != null ? toilet.getDistanceMeters().doubleValue() : null;
        String text = formatterService.toiletDetail(toilet, distanceMeters);
        sender.editOrReply(ctx, text, inlineKeyboard.toiletDetail(toilet));
        log.info(
            "Handled detail callback: chatId={}, userId={}, restroomId={}, outcome=detail_sent",
            ctx.chatId(),
            ctx.userId(),
            toiletId
        );
    }
}
