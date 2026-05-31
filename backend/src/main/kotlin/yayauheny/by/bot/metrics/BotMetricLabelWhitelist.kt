package yayauheny.by.bot.metrics

import java.util.Locale

object BotMetricLabelWhitelist {
    const val UNKNOWN = "unknown"
    private val UPDATE_TYPES = setOf("message", "callback", "command", "location", "other")
    private val OUTCOMES = setOf("success", "4xx", "5xx", "error")
    private val CACHE_OUTCOMES = setOf("hit", "miss", "expired")
    private val CLIENT_TYPES = setOf("telegram_bot", "telegram_miniapp", "api")
    private val ENDPOINTS = setOf("/restrooms/nearest", "/restrooms/{id}", UNKNOWN)

    fun normalizeUpdateType(type: String?): String {
        return normalize(type, UPDATE_TYPES, "other")
    }

    fun normalizeOutcome(outcome: String?): String {
        return normalize(outcome, OUTCOMES, "error")
    }

    fun normalizeCacheOutcome(outcome: String?): String {
        return normalize(outcome, CACHE_OUTCOMES, "miss")
    }

    fun normalizeClientType(clientType: String?): String {
        return normalize(clientType, CLIENT_TYPES, "telegram_bot")
    }

    fun normalizeEndpoint(endpoint: String?): String {
        return normalize(endpoint, ENDPOINTS, UNKNOWN)
    }

    private fun normalize(value: String?, allowed: Set<String>, fallback: String): String {
        if (value == null) return fallback
        val normalized = value.trim().lowercase(Locale.ROOT)
        return if (allowed.contains(normalized)) normalized else fallback
    }
}
