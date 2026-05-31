package yayauheny.by.common.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.web.filter.OncePerRequestFilter
import java.security.MessageDigest

@Order(Ordered.HIGHEST_PRECEDENCE)
class TelegramWebhookSecretTokenFilter(
    private val telegramBotProperties: TelegramBotProperties
) : OncePerRequestFilter() {

    companion object {
        private const val SECRET_TOKEN_HEADER = "X-Telegram-Bot-Api-Secret-Token"
    }

    private val webhookPath = normalizeWebhookPath(telegramBotProperties.webhookPath)

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        if (!"POST".equals(request.method, ignoreCase = true)) {
            return true
        }
        return !isWebhookPath(request)
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val secretToken = request.getHeader(SECRET_TOKEN_HEADER)
        val webhookSecretToken = telegramBotProperties.webhookSecretToken

        if (webhookSecretToken.isBlank() || secretToken.isNullOrBlank()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        if (!MessageDigest.isEqual(
                webhookSecretToken.toByteArray(Charsets.UTF_8),
                secretToken.toByteArray(Charsets.UTF_8)
            )
        ) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun isWebhookPath(request: HttpServletRequest): Boolean {
        var requestPath = request.requestURI
        val contextPath = request.contextPath
        if (!contextPath.isNullOrBlank() && requestPath.startsWith(contextPath)) {
            requestPath = requestPath.substring(contextPath.length)
        }
        return normalizeWebhookPath(requestPath) == webhookPath
    }

    private fun normalizeWebhookPath(path: String?): String {
        if (path == null) {
            return ""
        }
        var normalized = path.trim()
        if (normalized.isEmpty()) {
            return normalized
        }
        if (!normalized.startsWith("/")) {
            normalized = "/$normalized"
        }
        while (normalized.length > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length - 1)
        }
        return normalized
    }
}
