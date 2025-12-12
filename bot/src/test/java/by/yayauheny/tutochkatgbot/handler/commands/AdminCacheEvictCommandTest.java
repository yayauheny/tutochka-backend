package by.yayauheny.tutochkatgbot.handler.commands;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.config.AdminProperties;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

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
    void canHandleSupportsBothCommandsForAdmin() {
        Update geo = commandUpdate("/evict-geo", 1L);
        Update info = commandUpdate("/evict-info", 1L);
        Update other = commandUpdate("/start", 1L);
        Update nonAdmin = commandUpdate("/evict-geo", 99L);

        assertTrue(handler.canHandle(geo));
        assertTrue(handler.canHandle(info));
        assertFalse(handler.canHandle(other));
        // Non-admin should not be able to handle admin commands
        assertFalse(handler.canHandle(nonAdmin));
    }

    @Test
    void authorizedUserCanEvictGeoCache() {
        Update update = commandUpdate("/evict-geo", 1L);
        UpdateContext ctx = new UpdateContext(10L, 1L, "/evict-geo", false, null, null, false, null, 1);

        handler.handle(update, ctx);

        verify(cacheService, times(1)).evictGeo();
        verify(sender, times(1)).sendText(10L, "Кэш ближайших туалетов очищен.");
        verifyNoMoreInteractions(cacheService);
    }

    @Test
    void authorizedUserCanEvictInfoCache() {
        Update update = commandUpdate("/evict-info", 1L);
        UpdateContext ctx = new UpdateContext(10L, 1L, "/evict-info", false, null, null, false, null, 1);

        handler.handle(update, ctx);

        verify(cacheService, times(1)).evictInfo();
        verify(sender, times(1)).sendText(10L, "Кэш информации о туалетах очищен.");
    }

    private Update commandUpdate(String text, long userId) {
        Update update = mock(Update.class);
        Message message = mock(Message.class);
        User user = mock(User.class);
        
        // Create MessageEntity for bot command
        MessageEntity commandEntity = MessageEntity.builder()
                .type("bot_command")
                .offset(0)
                .length(text.length())
                .build();
        
        when(update.hasMessage()).thenReturn(true);
        when(update.getMessage()).thenReturn(message);
        when(message.getText()).thenReturn(text);
        when(message.getEntities()).thenReturn(List.of(commandEntity));
        when(message.getFrom()).thenReturn(user);
        when(user.getId()).thenReturn(userId);
        return update;
    }
}
