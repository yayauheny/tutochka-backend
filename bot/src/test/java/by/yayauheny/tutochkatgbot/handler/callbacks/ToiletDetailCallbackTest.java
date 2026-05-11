package by.yayauheny.tutochkatgbot.handler.callbacks;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import yayauheny.by.contract.BackendClient;
import yayauheny.by.model.enums.AccessibilityType;
import yayauheny.by.model.enums.DataSourceType;
import yayauheny.by.model.enums.FeeType;
import yayauheny.by.model.enums.ImportProvider;
import yayauheny.by.model.dto.Coordinates;
import yayauheny.by.model.enums.LocationType;
import yayauheny.by.model.enums.PlaceType;
import yayauheny.by.model.restroom.RestroomResponseDto;
import yayauheny.by.model.enums.RestroomStatus;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.keyboard.InlineKeyboardFactory;
import by.yayauheny.tutochkatgbot.messages.Messages;
import by.yayauheny.tutochkatgbot.service.FormatterService;
import by.yayauheny.tutochkatgbot.service.SearchService;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import kotlinx.serialization.json.JsonObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import yayauheny.by.model.analytics.AnalyticsEventRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.isNull;

class ToiletDetailCallbackTest {
    @Mock
    private MessageSender sender;
    @Mock
    private SearchService searchService;
    @Mock
    private BackendClient backendClient;

    private FormatterService formatterService;
    private InlineKeyboardFactory inlineKeyboard;
    private ToiletDetailCallback handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        formatterService = new FormatterService();
        inlineKeyboard = new InlineKeyboardFactory(formatterService);
        handler = new ToiletDetailCallback(sender, searchService, backendClient, formatterService, inlineKeyboard);
    }

    @Test
    void shouldRenderDetailWithKeyboardForValidId() throws Exception {
        UUID restroomId = UUID.randomUUID();
        when(searchService.getById(restroomId.toString())).thenReturn(Optional.of(sampleRestroom(restroomId)));

        Update update = createDetailUpdate("detail:" + restroomId);
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<InlineKeyboardMarkup> keyboardCaptor = ArgumentCaptor.forClass(InlineKeyboardMarkup.class);

        verify(searchService).getById(restroomId.toString());
        verify(sender).editOrReply(eq(ctx), textCaptor.capture(), keyboardCaptor.capture());
        verify(backendClient).trackProductEvent(org.mockito.ArgumentMatchers.any(AnalyticsEventRequest.class), eq(10L), eq(123L), eq("test-user-10"));
        assertThat(textCaptor.getValue()).contains("Test restroom");
        assertThat(keyboardCaptor.getValue().getKeyboard()).isNotEmpty();
        assertThat(keyboardCaptor.getValue().getKeyboard().stream().flatMap(row -> row.stream()).anyMatch(button -> button.getUrl() != null))
            .isTrue();
    }

    @Test
    void shouldRejectEmptyIdWithoutBuildingKeyboard() throws Exception {
        Update update = createDetailUpdate("detail:");
        UpdateContext ctx = UpdateContext.from(update);

        handler.handle(update, ctx);

        verify(searchService, never()).getById(anyString());
        verify(sender).editOrReply(eq(ctx), eq(Messages.SOMETHING_WENT_WRONG), isNull());
    }

    @ParameterizedTest
    @ValueSource(strings = {"detail:abc", "detail:-1"})
    void shouldPropagateMalformedOrNegativeId(String callbackData) throws Exception {
        Update update = createDetailUpdate(callbackData);
        UpdateContext ctx = UpdateContext.from(update);

        when(searchService.getById(anyString())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> handler.handle(update, ctx));

        verify(searchService).getById(callbackData.substring(callbackData.indexOf(':') + 1));
        verify(sender, never()).editOrReply(eq(ctx), eq(Messages.SOMETHING_WENT_WRONG), isNull());
    }

    @Test
    void shouldPropagateWhenSearchFindsNothing() throws Exception {
        UUID restroomId = UUID.randomUUID();
        when(searchService.getById(restroomId.toString())).thenReturn(Optional.empty());

        Update update = createDetailUpdate("detail:" + restroomId);
        UpdateContext ctx = UpdateContext.from(update);

        assertThrows(IllegalArgumentException.class, () -> handler.handle(update, ctx));

        verify(searchService).getById(restroomId.toString());
        verify(sender, never()).editOrReply(eq(ctx), eq(Messages.SOMETHING_WENT_WRONG), isNull());
    }

    private Update createDetailUpdate(String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(Chat.builder().id(123L).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(10L).isBot(false).firstName("Test").userName("test-user-10").build());
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private RestroomResponseDto sampleRestroom(UUID restroomId) {
        return new RestroomResponseDto(
            restroomId,
            UUID.randomUUID(),
            "Minsk",
            null,
            null,
            "Test restroom",
            "Address",
            new JsonObject(Map.of()),
            new JsonObject(Map.of()),
            FeeType.FREE,
            null,
            AccessibilityType.UNKNOWN,
            PlaceType.OTHER,
            new Coordinates(53.9, 27.56),
            DataSourceType.USER,
            RestroomStatus.ACTIVE,
            new JsonObject(Map.of()),
            new JsonObject(Map.of("2gis", kotlinx.serialization.json.JsonElementKt.JsonPrimitive("abc123"))),
            null,
            null,
            false,
            false,
            LocationType.UNKNOWN,
            ImportProvider.USER,
            null,
            false,
            Instant.parse("2025-01-01T00:00:00Z"),
            Instant.parse("2025-01-01T00:00:00Z"),
            100,
            null,
            null
        );
    }
}
