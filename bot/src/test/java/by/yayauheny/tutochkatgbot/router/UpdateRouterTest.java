package by.yayauheny.tutochkatgbot.router;

import by.yayauheny.tutochkatgbot.bot.MessageSender;
import by.yayauheny.tutochkatgbot.handler.CallbackHandler;
import by.yayauheny.tutochkatgbot.handler.CommandHandler;
import by.yayauheny.tutochkatgbot.handler.MessageHandler;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import by.yayauheny.tutochkatgbot.messages.Messages;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.message.Message;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateRouterTest {
    @Mock
    private CommandHandler commandHandler;
    @Mock
    private CallbackHandler firstCallbackHandler;
    @Mock
    private CallbackHandler secondCallbackHandler;
    @Mock
    private MessageHandler messageHandler;
    @Mock
    private MessageSender sender;

    private UpdateRouter router;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        router =
            new UpdateRouter(
                List.of(commandHandler),
                List.of(firstCallbackHandler, secondCallbackHandler),
                List.of(messageHandler),
                sender
            );
    }

    @Test
    void routeShouldDispatchCallbackToFirstMatchingHandlerAndSkipOthers() throws Exception {
        Update update = createCallbackUpdate("detail:123");
        UpdateContext ctx = UpdateContext.from(update);

        when(firstCallbackHandler.canHandle("detail:123")).thenReturn(true);
        when(secondCallbackHandler.canHandle("detail:123")).thenReturn(true);

        router.route(update);

        verify(sender).answerCallbackQuery("query-id", null);
        verify(firstCallbackHandler).handle(update, ctx);
        verify(secondCallbackHandler, never()).handle(update, ctx);
        verify(commandHandler, never()).handle(eq(update), eq(ctx));
        verify(messageHandler, never()).handle(eq(update), eq(ctx));
    }

    @Test
    void routeShouldDispatchCommandAndSkipMessageHandlers() throws Exception {
        Update update = createCommandUpdate("/start");
        UpdateContext ctx = UpdateContext.from(update);

        when(commandHandler.canHandle(update)).thenReturn(true);

        router.route(update);

        verify(commandHandler).handle(update, ctx);
        verify(messageHandler, never()).handle(eq(update), eq(ctx));
        verify(firstCallbackHandler, never()).handle(eq(update), eq(ctx));
    }

    @Test
    void routeShouldDispatchPlainMessageToMessageHandler() throws Exception {
        Update update = createPlainTextUpdate("hello");
        UpdateContext ctx = UpdateContext.from(update);

        when(messageHandler.canHandle(update, ctx)).thenReturn(true);

        router.route(update);

        verify(messageHandler).handle(update, ctx);
        verify(commandHandler, never()).handle(eq(update), eq(ctx));
    }

    @Test
    void routeShouldSendFallbackForUnknownCallback() throws Exception {
        Update update = createCallbackUpdate("unknown:123");

        when(firstCallbackHandler.canHandle(anyString())).thenReturn(false);
        when(secondCallbackHandler.canHandle(anyString())).thenReturn(false);

        router.route(update);

        verify(sender).answerCallbackQuery("query-id", null);
        verify(sender).safeReply(UpdateContext.from(update), "Действие недоступно. Попробуй ещё раз или начни с /start.");
        verify(firstCallbackHandler, never()).handle(eq(update), org.mockito.ArgumentMatchers.any(UpdateContext.class));
        verify(secondCallbackHandler, never()).handle(eq(update), org.mockito.ArgumentMatchers.any(UpdateContext.class));
    }

    @Test
    void routeShouldSendUnknownMessageFallbackForPlainTextWithoutHandlers() throws Exception {
        Update update = createPlainTextUpdate("hello");

        router.route(update);

        verify(sender).safeReply(UpdateContext.from(update), Messages.UNKNOWN_MESSAGE);
        verify(commandHandler, never()).handle(eq(update), org.mockito.ArgumentMatchers.any(UpdateContext.class));
        verify(messageHandler, never()).handle(eq(update), org.mockito.ArgumentMatchers.any(UpdateContext.class));
    }

    @Test
    void routeShouldSendSomethingWentWrongOnceWhenCallbackHandlerThrows() throws Exception {
        Update update = createCallbackUpdate("detail:123");
        UpdateContext ctx = UpdateContext.from(update);

        when(firstCallbackHandler.canHandle("detail:123")).thenReturn(true);
        org.mockito.Mockito.doThrow(new RuntimeException("boom")).when(firstCallbackHandler).handle(update, ctx);

        router.route(update);

        verify(sender).answerCallbackQuery("query-id", null);
        verify(sender).safeReply(eq(ctx), eq(Messages.SOMETHING_WENT_WRONG));
        verify(sender, never()).sendText(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyString());
        verify(sender, never()).editOrReply(eq(ctx), org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any());
        verify(secondCallbackHandler, never()).handle(eq(update), eq(ctx));
    }

    private Update createCallbackUpdate(String callbackData) {
        Update update = new Update();
        CallbackQuery callbackQuery = new CallbackQuery();
        callbackQuery.setData(callbackData);
        callbackQuery.setId("query-id");
        Message message = new Message();
        message.setMessageId(1);
        message.setChat(Chat.builder().id(123L).type("private").build());
        callbackQuery.setMessage(message);
        callbackQuery.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        update.setCallbackQuery(callbackQuery);
        return update;
    }

    private Update createCommandUpdate(String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        message.setChat(Chat.builder().id(123L).type("private").build());
        message.setMessageId(1);
        message.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        message.setEntities(
            List.of(
                MessageEntity.builder()
                    .type("bot_command")
                    .offset(0)
                    .length(text.length())
                    .build()
            )
        );
        update.setMessage(message);
        return update;
    }

    private Update createPlainTextUpdate(String text) {
        Update update = new Update();
        Message message = new Message();
        message.setText(text);
        message.setChat(Chat.builder().id(123L).type("private").build());
        message.setMessageId(1);
        message.setFrom(User.builder().id(10L).isBot(false).firstName("Test").build());
        update.setMessage(message);
        return update;
    }

}
