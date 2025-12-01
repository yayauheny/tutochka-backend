package by.yayauheny.tutochkatgbot.ingress;

import by.yayauheny.tutochkatgbot.config.TelegramProperties;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

/**
 * Long polling implementation of UpdateIngress
 */
@Component
@ConditionalOnProperty(name = "bot.mode", havingValue = "POLLING", matchIfMissing = true)
public class LongPollingIngress extends TelegramLongPollingBot implements UpdateIngress {
    
    private final TelegramProperties telegramProperties;
    private final UpdateRouter router;
    
    public LongPollingIngress(TelegramProperties telegramProperties, UpdateRouter router) {
        this.telegramProperties = telegramProperties;
        this.router = router;
    }
    
    @Override
    public String getBotUsername() {
        return telegramProperties.username();
    }
    
    @Override
    public String getBotToken() {
        return telegramProperties.token();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        router.route(update);
    }
    
    @Override
    public void start() {
    }
    
    @Override
    public void stop() {
    }
}
