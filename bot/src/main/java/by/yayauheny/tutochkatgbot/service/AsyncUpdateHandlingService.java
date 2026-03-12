package by.yayauheny.tutochkatgbot.service;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Asynchronous update handling service wrapper
 */
@Service
public class AsyncUpdateHandlingService {
    private final UpdateHandlingService sync;

    public AsyncUpdateHandlingService(UpdateHandlingService sync) {
        this.sync = sync;
    }

    @Async("telegramUpdateExecutor")
    public void handle(Update update) {
        sync.handle(update);
    }
}

