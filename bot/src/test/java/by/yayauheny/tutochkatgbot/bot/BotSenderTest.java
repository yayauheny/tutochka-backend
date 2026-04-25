package by.yayauheny.tutochkatgbot.bot;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

class BotSenderTest {

    @Test
    void sendTextShouldWrapTelegramApiException() throws TelegramApiException {
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        BotSender sender = new BotSender(telegramClient);
        doThrow(new TelegramApiException("boom")).when(telegramClient).execute(any(SendMessage.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sender.sendText(123L, "hello"));

        assertTrue(ex.getMessage().contains("chat 123"));
    }

    @Test
    void answerCallbackQueryShouldWrapTelegramApiException() throws TelegramApiException {
        TelegramClient telegramClient = Mockito.mock(TelegramClient.class);
        BotSender sender = new BotSender(telegramClient);
        doThrow(new TelegramApiException("boom")).when(telegramClient).execute(any(AnswerCallbackQuery.class));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> sender.answerCallbackQuery("callback-id", null));

        assertTrue(ex.getMessage().contains("callback-id"));
    }
}
