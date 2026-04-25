package by.yayauheny.tutochkatgbot.router;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.*;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.util.CommandUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UpdateRouter {
    private final List<CommandHandler> commandHandlers;
    private final List<CallbackHandler> callbackHandlers;
    private final List<MessageHandler> messageHandlers;
    private final MessageSender sender;

    public UpdateRouter(List<CommandHandler> commandHandlers,
                       List<CallbackHandler> callbackHandlers,
                       List<MessageHandler> messageHandlers,
                       MessageSender sender) {
        this.commandHandlers = commandHandlers.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .collect(Collectors.toList());
        this.callbackHandlers = callbackHandlers.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .collect(Collectors.toList());
        this.messageHandlers = messageHandlers.stream()
                .sorted(AnnotationAwareOrderComparator.INSTANCE)
                .collect(Collectors.toList());
        this.sender = sender;
    }

    public void route(Update update) throws Exception {
        UpdateContext ctx = UpdateContext.from(update);

        if (update.hasCallbackQuery()) {
            sender.answerCallbackQuery(update.getCallbackQuery().getId(), null);
            handleCallback(update, ctx);
            return;
        }
        
        if (update.hasMessage() && CommandUtils.isCommand(update.getMessage())) {
            handleCommand(update, ctx);
            return;
        }
        
        handleMessage(update, ctx);
    }
    
    private void handleCallback(Update update, UpdateContext ctx) throws Exception {
        String callbackData = ctx.callbackData();
        if (callbackData == null) {
            return;
        }

        for (CallbackHandler handler : callbackHandlers) {
            if (handler.canHandle(callbackData)) {
                handler.handle(update, ctx);
                return;
            }
        }

        sender.safeReply(ctx, "Действие недоступно. Попробуй ещё раз или начни с /start.");
    }
    
    private void handleCommand(Update update, UpdateContext ctx) throws Exception {
        String command = CommandUtils.extractCommand(update.getMessage());
        if (command == null) {
            sender.safeReply(ctx, Messages.UNKNOWN_COMMAND);
            return;
        }

        for (CommandHandler handler : commandHandlers) {
            if (handler.canHandle(update)) {
                handler.handle(update, ctx);
                return;
            }
        }

        sender.safeReply(ctx, Messages.UNKNOWN_COMMAND);
    }
    
    private void handleMessage(Update update, UpdateContext ctx) throws Exception {
        for (MessageHandler handler : messageHandlers) {
            if (handler.canHandle(update, ctx)) {
                handler.handle(update, ctx);
                return;
            }
        }

        boolean isCmd = update.hasMessage() && CommandUtils.isCommand(update.getMessage());
        String fallbackMessage = isCmd
            ? Messages.UNKNOWN_COMMAND
            : Messages.UNKNOWN_MESSAGE;
        sender.safeReply(ctx, fallbackMessage);
    }
}
