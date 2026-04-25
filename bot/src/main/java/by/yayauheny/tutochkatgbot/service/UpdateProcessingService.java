package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateProcessingService {
    private static final Logger log = LoggerFactory.getLogger(UpdateProcessingService.class);

    private final UpdateHandlingService updateHandlingService;

    public UpdateProcessingService(UpdateHandlingService updateHandlingService) {
        this.updateHandlingService = updateHandlingService;
    }

    public void process(Update update) throws Exception {
        UpdateContext ctx = UpdateContext.from(update);
        String updateType = updateHandlingService.resolveUpdateType(update);
        long startedAt = System.nanoTime();
        log.info(
            "Webhook update received: updateId={}, type={}, chatId={}, userId={}",
            update.getUpdateId(),
            updateType,
            ctx.chatId(),
            ctx.userId()
        );
        updateHandlingService.handle(update);
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info(
            "Webhook update processed: updateId={}, type={}, chatId={}, userId={}, durationMs={}",
            update.getUpdateId(),
            updateType,
            ctx.chatId(),
            ctx.userId(),
            durationMs
        );
    }
}
