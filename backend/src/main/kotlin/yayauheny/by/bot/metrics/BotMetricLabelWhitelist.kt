package yayauheny.by.bot.metrics

import java.util.Locale

object BotMetricLabelWhitelist {
    private val UPDATE_TYPES = setOf("message", "callback", "command", "location", "other")
    private val CACHE_OUTCOMES = setOf("hit", "miss", "expired")

    fun normalizeUpdateType(type: String?): String {
        return normalize(type, UPDATE_TYPES, "other")
    }

    fun normalizeCacheOutcome(outcome: String?): String {
        return normalize(outcome, CACHE_OUTCOMES, "miss")
    }

    private fun normalize(value: String?, allowed: Set<String>, fallback: String): String {
        if (value == null) return fallback
        val normalized = value.trim().lowercase(Locale.ROOT)
        return if (allowed.contains(normalized)) normalized else fallback
    }
}
