package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.config.AdminProperties;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class AdminCacheEvictCommandTest {

    private RestroomCacheService cacheService;
    private MessageSender sender;
    private AdminCacheEvictCommand handler;

    @BeforeEach
    void setUp() {
        cacheService = mock(RestroomCacheService.class);
        sender = mock(MessageSender.class);
        handler = new AdminCacheEvictCommand(cacheService, sender, new AdminProperties(List.of("1", "2")));
    }

    @Test
    void canHandleSupportsBothCommands() {
        Update geo = commandUpdate("/evict-geo");
        Update info = commandUpdate("/evict-info");
        Update other = commandUpdate("/start");

        assertTrue(handler.canHandle(geo));
        assertTrue(handler.canHandle(info));
        assertFalse(handler.canHandle(other));
    }

    @Test
    void authorizedUserCanEvictGeoCache() {
        Update update = commandUpdate("/evict-geo");
        UpdateContext ctx = new UpdateContext(10L, 1L, "/evict-geo", false, null, null, false, null, 1);

        handler.handle(update, ctx);

        verify(cacheService, times(1)).evictGeo();
        verify(sender, times(1)).sendText(10L, "Кэш ближайших туалетов очищен.");
        verifyNoMoreInteractions(cacheService);
    }

    @Test
    void authorizedUserCanEvictInfoCache() {
        Update update = commandUpdate("/evict-info");
        UpdateContext ctx = new UpdateContext(10L, 1L, "/evict-info", false, null, null, false, null, 1);

        handler.handle(update, ctx);

        verify(cacheService, times(1)).evictInfo();
        verify(sender, times(1)).sendText(10L, "Кэш информации о туалетах очищен.");
    }

    @Test
    void unauthorizedUserGetsDenial() {
        Update update = commandUpdate("/evict-geo");
        UpdateContext ctx = new UpdateContext(10L, 99L, "/evict-geo", false, null, null, false, null, 1);

        handler.handle(update, ctx);

        verifyNoInteractions(cacheService);
        verify(sender, times(1)).sendText(10L, "Команда недоступна.");
    }

    private Update commandUpdate(String text) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(text);
        return update;
    }
}
