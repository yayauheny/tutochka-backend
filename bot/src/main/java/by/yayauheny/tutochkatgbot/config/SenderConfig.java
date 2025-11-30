package by.yayauheny.tutochkatgbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.telegram.telegrambots.bots.DefaultAbsSender;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Configuration for independent message sender
 * This breaks the circular dependency between BotSender and LongPollingIngress
 */
@Configuration
public class SenderConfig {

    @Bean(name = "botAbsSender")
    @Primary
    public AbsSender botAbsSender(TelegramProperties props) {
        DefaultBotOptions options = new DefaultBotOptions();
        return new DefaultAbsSender(options) {
            @Override
            public String getBotToken() {
                return props.token();
            }
        };
    }
}
