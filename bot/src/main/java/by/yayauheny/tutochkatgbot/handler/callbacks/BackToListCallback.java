package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import by.yayauheny.tutochkatgbot.callback.CallbackData;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Handler for back to list callback (back:list)
 */
@Component
@Order(2)
public class BackToListCallback implements CallbackHandler {
    private final MessageSender sender;
    private final BackListSnapshotCache backListSnapshotCache;
    private final FormatterService formatterService;
    private final InlineKeyboardFactory inlineKeyboard;
    private final ReplyKeyboardFactory replyKeyboard;

    public BackToListCallback(MessageSender sender,
                             BackListSnapshotCache backListSnapshotCache,
                             FormatterService formatterService,
                             InlineKeyboardFactory inlineKeyboard,
                             ReplyKeyboardFactory replyKeyboard) {
        this.sender = sender;
        this.backListSnapshotCache = backListSnapshotCache;
        this.formatterService = formatterService;
        this.inlineKeyboard = inlineKeyboard;
        this.replyKeyboard = replyKeyboard;
    }

    @Override
    public String prefix() {
        return "back";
    }

    @Override
    public boolean canHandle(String callbackData) {
        return CallbackData.isType(callbackData, "back") && "list".equals(CallbackData.arg(callbackData));
    }

    @Override
    public void handle(Update update, UpdateContext ctx) throws Exception {
        var snapshotOpt = backListSnapshotCache.get(ctx.chatId(), ctx.userId());
        if (snapshotOpt.isEmpty()) {
            sender.sendText(ctx.chatId(), Messages.LOCATION_REQUEST, replyKeyboard.shareLocation());
            return;
        }

        var snapshot = snapshotOpt.get();
        String message = formatterService.toiletsFound(snapshot.items().size());
        sender.editOrReply(ctx, message, inlineKeyboard.toiletList(snapshot.items()));
    }
}
