package by.yayauheny.tutochkatgbot.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.test.util.ReflectionTestUtils;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class WebhookRegistrarTest {

    @Test
    void runShouldSkipRegistrationWhenWebhookUrlIsBlank() throws Exception {
        TelegramClient client = mock(TelegramClient.class);
        WebhookRegistrar registrar = new WebhookRegistrar(client);
        ReflectionTestUtils.setField(registrar, "webhookUrl", " ");

        registrar.run(new DefaultApplicationArguments(new String[0]));

        verify(client, never()).execute(any(SetWebhook.class));
    }

    @Test
    void runShouldRegisterWebhookWhenUrlIsConfigured() throws Exception {
        TelegramClient client = mock(TelegramClient.class);
        WebhookRegistrar registrar = new WebhookRegistrar(client);
        ReflectionTestUtils.setField(registrar, "webhookUrl", "https://example.test/webhook");

        registrar.run(new DefaultApplicationArguments(new String[0]));

        ArgumentCaptor<SetWebhook> captor = ArgumentCaptor.forClass(SetWebhook.class);
        verify(client).execute(captor.capture());
        assertThat(captor.getValue().getUrl()).isEqualTo("https://example.test/webhook");
    }
}
