package by.yayauheny.tutochkatgbot.config;

import by.yayauheny.tutochkatgbot.ingress.LongPollingIngress;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

/**
 * Configuration for bot ingress (polling/webhook) and registration
 */
@Configuration
public class BotIngressConfig {

    @Bean
    @ConditionalOnBean(LongPollingIngress.class)
    @ConditionalOnProperty(name = "bot.auto-register", havingValue = "true", matchIfMissing = false)
    public TelegramBotsApi telegramBotsApi(LongPollingIngress longPollingIngress) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(longPollingIngress);
        return api;
    }
}
