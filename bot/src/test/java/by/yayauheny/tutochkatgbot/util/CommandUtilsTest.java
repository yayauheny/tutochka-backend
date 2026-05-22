package by.yayauheny.tutochkatgbot.util;

import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CommandUtilsTest {
    @Test
    void extractCommandArgumentShouldReturnStartPayload() {
        Message message = new Message();
        message.setText("/start website");
        message.setEntities(
            List.of(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length("/start".length())
                    .build()
            )
        );

        assertThat(CommandUtils.extractCommandArgument(message)).isEqualTo("website");
    }

    @Test
    void extractCommandArgumentShouldReturnNullWhenPayloadMissing() {
        Message message = new Message();
        message.setText("/start");
        message.setEntities(
            List.of(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length("/start".length())
                    .build()
            )
        );

        assertThat(CommandUtils.extractCommandArgument(message)).isNull();
    }
}
