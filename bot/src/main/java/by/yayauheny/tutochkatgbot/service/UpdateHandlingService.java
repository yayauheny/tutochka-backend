package by.yayauheny.tutochkatgbot.service;

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

    public UpdateHandlingService(UpdateRouter router) {
        this.router = router;
    }

    public void handle(Update update) {
        try {
            router.route(update);
        } catch (Exception e) {
            log.error("Error processing update", e);
        }
    }
}

