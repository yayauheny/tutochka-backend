package by.yayauheny.tutochkatgbot.config;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.ApplicationArguments;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class WebhookRegistrarTest {

    @Test
    void runShouldSkipRegistrationWhenWebhookUrlIsBlank() throws Exception {
        TelegramClient client = Mockito.mock(TelegramClient.class);
        WebhookRegistrar registrar = new WebhookRegistrar(client, "", "secret-token");

        registrar.run(Mockito.mock(ApplicationArguments.class));

        verifyNoInteractions(client);
    }

    @Test
    void runShouldRegisterWebhookWithSecretToken() throws Exception {
        TelegramClient client = Mockito.mock(TelegramClient.class);
        WebhookRegistrar registrar = new WebhookRegistrar(client, "https://example.com/webhook", "secret-token");

        registrar.run(Mockito.mock(ApplicationArguments.class));

        ArgumentCaptor<SetWebhook> captor = ArgumentCaptor.forClass(SetWebhook.class);
        verify(client).execute(captor.capture());
        assertThat(captor.getValue().getUrl()).isEqualTo("https://example.com/webhook");
        assertThat(captor.getValue().getSecretToken()).isEqualTo("secret-token");
    }

    @Test
    void runShouldFailWhenWebhookUrlIsConfiguredWithoutSecretToken() {
        TelegramClient client = Mockito.mock(TelegramClient.class);
        WebhookRegistrar registrar = new WebhookRegistrar(client, "https://example.com/webhook", "");

        assertThatThrownBy(() -> registrar.run(Mockito.mock(ApplicationArguments.class)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("bot.webhook-secret-token must be configured when bot.webhook-public-url is set");

        verifyNoInteractions(client);
    }
}
