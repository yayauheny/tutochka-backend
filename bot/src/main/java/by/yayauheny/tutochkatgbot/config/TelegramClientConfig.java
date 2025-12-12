package by.yayauheny.tutochkatgbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/**
 * Configuration for Telegram client
 */
@Configuration
public class TelegramClientConfig {

    @Bean
    public TelegramClient telegramClient(TelegramProperties props) {
        return new OkHttpTelegramClient(props.token());
    }
}

