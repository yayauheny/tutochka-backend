package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.router.UpdateRouter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.location.Location;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateHandlingServiceTest {

    private final UpdateRouter router = Mockito.mock(UpdateRouter.class);
    private final BotMetrics botMetrics = Mockito.mock(BotMetrics.class);
    private final UpdateHandlingService service = new UpdateHandlingService(router, botMetrics);

    @Test
    void resolveUpdateType_shouldReturnCallback() {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData("detail:abc");
        update.setCallbackQuery(callbackQuery);

        assertEquals("callback", service.resolveUpdateType(update));
    }

    @Test
    void resolveUpdateType_shouldReturnLocation() {
        Update update = new Update();
        Message message = new Message();
        message.setChat(Chat.builder().id(1L).type("private").build());
        message.setLocation(Location.builder().latitude(53.9).longitude(27.56).build());
        update.setMessage(message);

        assertEquals("location", service.resolveUpdateType(update));
    }

    @Test
    void resolveUpdateType_shouldReturnCommand() {
        Update update = new Update();
        Message message = new Message();
        message.setChat(Chat.builder().id(1L).type("private").build());
        message.setText("/start");
        update.setMessage(message);

        assertEquals("command", service.resolveUpdateType(update));
    }

    @Test
    void resolveUpdateType_shouldReturnMessage() {
        Update update = new Update();
        Message message = new Message();
        message.setChat(Chat.builder().id(1L).type("private").build());
        message.setText("hello");
        update.setMessage(message);

        assertEquals("message", service.resolveUpdateType(update));
    }

    @Test
    void resolveUpdateType_shouldReturnOther() {
        assertEquals("other", service.resolveUpdateType(new Update()));
    }
}
