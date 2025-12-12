package by.yayauheny.tutochkatgbot;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan("by.yayauheny.tutochkatgbot.config")
@EnableAsync
public class TutochkaTgBotApplication {

  public static void main(String[] args) {
    SpringApplication.run(TutochkaTgBotApplication.class, args);
  }

}
