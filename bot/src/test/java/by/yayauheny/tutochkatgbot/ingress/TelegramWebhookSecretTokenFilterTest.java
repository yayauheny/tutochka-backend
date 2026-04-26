package by.yayauheny.tutochkatgbot.ingress;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.FilterChain;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class TelegramWebhookSecretTokenFilterTest {

    @Test
    void filterShouldRejectWebhookRequestWithoutSecretToken() throws Exception {
        TelegramWebhookSecretTokenFilter filter = new TelegramWebhookSecretTokenFilter("/telegram/webhook", "secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/telegram/webhook");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (FilterChain) (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertThat(chainInvoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void filterShouldRejectWebhookRequestWithWrongSecretToken() throws Exception {
        TelegramWebhookSecretTokenFilter filter = new TelegramWebhookSecretTokenFilter("/telegram/webhook", "secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/telegram/webhook");
        request.addHeader("X-Telegram-Bot-Api-Secret-Token", "wrong-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (FilterChain) (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertThat(chainInvoked).isFalse();
        assertThat(response.getStatus()).isEqualTo(403);
    }

    @Test
    void filterShouldAllowWebhookRequestWithValidSecretToken() throws Exception {
        TelegramWebhookSecretTokenFilter filter = new TelegramWebhookSecretTokenFilter("telegram/webhook/", "secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/telegram/webhook");
        request.addHeader("X-Telegram-Bot-Api-Secret-Token", "secret-token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (FilterChain) (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertThat(chainInvoked).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void filterShouldIgnoreNonWebhookPath() throws Exception {
        TelegramWebhookSecretTokenFilter filter = new TelegramWebhookSecretTokenFilter("/telegram/webhook", "secret-token");
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicBoolean chainInvoked = new AtomicBoolean(false);

        filter.doFilter(request, response, (FilterChain) (servletRequest, servletResponse) -> chainInvoked.set(true));

        assertThat(chainInvoked).isTrue();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
