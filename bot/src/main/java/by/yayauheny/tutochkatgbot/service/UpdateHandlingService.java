package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Synchronous update handling service
 */
@Service
public class UpdateHandlingService {
    private static final Logger log = LoggerFactory.getLogger(UpdateHandlingService.class);
    private final UpdateRouter router;
    private final BotMetrics botMetrics;

    public UpdateHandlingService(UpdateRouter router, BotMetrics botMetrics) {
        this.router = router;
        this.botMetrics = botMetrics;
    }

    public void handle(Update update) {
        botMetrics.incrementTelegramUpdate(resolveUpdateType(update));
        try {
            router.route(update);
        } catch (Exception e) {
            log.error("Error processing update", e);
            throw e;
        }
    }

    String resolveUpdateType(Update update) {
        if (update.hasCallbackQuery()) {
            return "callback";
        }
        if (update.hasMessage()) {
            if (update.getMessage().hasLocation()) {
                return "location";
            }
            if (update.getMessage().hasText() && update.getMessage().getText().startsWith("/")) {
                return "command";
            }
            return "message";
        }
        return "other";
    }
}
