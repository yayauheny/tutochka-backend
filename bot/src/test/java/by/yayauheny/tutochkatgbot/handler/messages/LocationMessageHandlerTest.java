package by.yayauheny.tutochkatgbot.handler.messages;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.cache.BackListSnapshotCache;
import yayauheny.by.model.enums.FeeType;
import yayauheny.by.model.dto.Coordinates;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.keyboard.ReplyKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import by.yayauheny.tutochkatgbot.service.UserService;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LocationMessageHandlerTest {
    @Mock
    private MessageSender sender;
    @Mock
    private SearchService searchService;
    @Mock
    private UserService userService;
    @Mock
    private BackListSnapshotCache backListSnapshotCache;

    private FormatterService formatterService;
    private InlineKeyboardFactory inlineKeyboard;
    private ReplyKeyboardFactory replyKeyboard;
    private LocationMessageHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatterService = new FormatterService();
        inlineKeyboard = new InlineKeyboardFactory(formatterService);
        replyKeyboard = new ReplyKeyboardFactory();
        handler = new LocationMessageHandler(sender, searchService, userService, backListSnapshotCache, formatterService, inlineKeyboard, replyKeyboard);
    }

    @Test
    void handleShouldSaveLocationBeforeSearchingAndStoreResults() throws Exception {
        UpdateContext ctx = new UpdateContext(123L, 456L, null, true, 53.9, 27.56, false, null, 1);
        List<NearestRestroomSlimDto> results = List.of(sampleItem("Test restroom"));
        when(searchService.findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT)).thenReturn(results);

        handler.handle(new Update(), ctx);

        ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);
        InOrder order = inOrder(userService, searchService, sender, backListSnapshotCache);
        order.verify(userService).saveLocation(456L, 53.9, 27.56);
        order.verify(searchService).findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);
        order.verify(sender).sendText(eq(123L), eq(formatterService.toiletsFound(1)), keyboardCaptor.capture());
        order.verify(backListSnapshotCache).store(123L, 456L, UserService.DEFAULT_RADIUS, results);

        assertThat(keyboardCaptor.getValue().getKeyboard()).isNotEmpty();
    }

    @Test
    void handleShouldSendNoResultsFlowWithoutStoringCache() throws Exception {
        UpdateContext ctx = new UpdateContext(123L, 456L, null, true, 53.9, 27.56, false, null, 1);
        when(searchService.findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT)).thenReturn(List.of());

        handler.handle(new Update(), ctx);

        verify(userService).saveLocation(456L, 53.9, 27.56);
        verify(searchService).findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);
        verify(sender).sendText(eq(123L), eq(Messages.NO_TOILETS_FOUND), org.mockito.ArgumentMatchers.any(InlineKeyboardMarkup.class));
        verify(sender).sendText(eq(123L), eq("Или попробуйте другую точку:"), org.mockito.ArgumentMatchers.any(org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup.class));
        verify(backListSnapshotCache, never()).store(anyLong(), anyLong(), anyInt(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void handleShouldSendErrorWhenCoordinatesMissingWithoutSearching() throws Exception {
        UpdateContext ctx = new UpdateContext(123L, 456L, null, true, null, null, false, null, 1);

        handler.handle(new Update(), ctx);

        verify(sender).sendText(123L, Messages.SOMETHING_WENT_WRONG);
        verify(userService, never()).saveLocation(anyLong(), anyDouble(), anyDouble());
        verify(searchService, never()).findNearby(anyDouble(), anyDouble(), anyInt(), anyInt());
        verify(backListSnapshotCache, never()).store(anyLong(), anyLong(), anyInt(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void handleShouldPropagateSearchExceptionWithoutPartialResponse() throws Exception {
        UpdateContext ctx = new UpdateContext(123L, 456L, null, true, 53.9, 27.56, false, null, 1);
        when(searchService.findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT))
            .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> handler.handle(new Update(), ctx));

        InOrder order = inOrder(userService, searchService);
        order.verify(userService).saveLocation(456L, 53.9, 27.56);
        order.verify(searchService).findNearby(53.9, 27.56, UserService.DEFAULT_RADIUS, SearchService.DEFAULT_NEAREST_LIMIT);

        verify(sender, never()).sendText(anyLong(), anyString());
        verify(backListSnapshotCache, never()).store(anyLong(), anyLong(), anyInt(), org.mockito.ArgumentMatchers.anyList());
    }

    private NearestRestroomSlimDto sampleItem(String name) {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            name,
            123.0,
            FeeType.FREE,
            new Coordinates(53.9, 27.56),
            new Coordinates(53.9001, 27.5601)
        );
    }
}
