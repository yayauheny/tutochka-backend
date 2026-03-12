package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.service.UpdateHandlingService;
import by.yayauheny.tutochkatgbot.service.AsyncUpdateHandlingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Service for update processing (sync or async based on configuration)
 */
@Service
public class UpdateProcessingService {
    private final boolean asyncEnabled;
    private final UpdateHandlingService sync;
    private final AsyncUpdateHandlingService async;

    public UpdateProcessingService(
            UpdateHandlingService sync,
            AsyncUpdateHandlingService async,
            @Value("${bot.async-processing:true}") boolean asyncEnabled
    ) {
        this.sync = sync;
        this.async = async;
        this.asyncEnabled = asyncEnabled;
    }

    public void process(Update update) {
        if (asyncEnabled) {
            async.handle(update);
        } else {
            sync.handle(update);
        }
    }
}
