package by.yayauheny.tutochkatgbot.ingress;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TelegramWebhookSecretTokenFilter extends OncePerRequestFilter {
    private static final String SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token";

    private final String webhookPath;
    private final String webhookSecretToken;

    public TelegramWebhookSecretTokenFilter(
            @Value("${bot.webhook-path}") String webhookPath,
            @Value("${bot.webhook-secret-token}") String webhookSecretToken
    ) {
        this.webhookPath = normalizeWebhookPath(webhookPath);
        this.webhookSecretToken = webhookSecretToken;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        return !isWebhookPath(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NotNull HttpServletResponse response,
            @NotNull FilterChain filterChain
    )
            throws ServletException, IOException {
        String secretToken = request.getHeader(SECRET_TOKEN_HEADER);
        if (webhookSecretToken == null || webhookSecretToken.isBlank() || secretToken == null || secretToken.isBlank()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        if (!MessageDigest.isEqual(
                webhookSecretToken.getBytes(StandardCharsets.UTF_8),
                secretToken.getBytes(StandardCharsets.UTF_8)
        )) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        filterChain.doFilter(request, response);
    }

    private boolean isWebhookPath(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length());
        }
        return normalizeWebhookPath(requestPath).equals(webhookPath);
    }

    private String normalizeWebhookPath(String path) {
        if (path == null) {
            return "";
        }
        String normalized = path.trim();
        if (normalized.isEmpty()) {
            return normalized;
        }
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        while (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
