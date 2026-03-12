package by.yayauheny.tutochkatgbot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuration for async processing
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "telegramUpdateExecutor")
    public Executor telegramUpdateExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(4);
        ex.setMaxPoolSize(16);
        ex.setQueueCapacity(1000);
        ex.setThreadNamePrefix("tg-upd-");
        ex.initialize();
        return ex;
    }
}

