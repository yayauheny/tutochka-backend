package by.yayauheny.tutochkatgbot;

import by.yayauheny.tutochkatgbot.config.AdminProperties;
import by.yayauheny.tutochkatgbot.config.TelegramProperties;
import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.tutochkatgbot.config.BotModeProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({TelegramProperties.class, BackendProperties.class, BotModeProperties.class, AdminProperties.class})
public class TutochkaTgBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(TutochkaTgBotApplication.class, args);
  }

}
