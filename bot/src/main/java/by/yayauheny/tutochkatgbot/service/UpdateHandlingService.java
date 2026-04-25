package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

@Service
public class UpdateHandlingService {
    private final UpdateRouter router;
    private final BotMetrics botMetrics;

    public UpdateHandlingService(UpdateRouter router, BotMetrics botMetrics) {
        this.router = router;
        this.botMetrics = botMetrics;
    }

    public void handle(Update update) throws Exception {
        botMetrics.incrementTelegramUpdate(resolveUpdateType(update));
        router.route(update);
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
