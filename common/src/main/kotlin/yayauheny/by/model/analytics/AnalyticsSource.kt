package yayauheny.by.model.analytics

import com.fasterxml.jackson.annotation.JsonProperty





enum class AnalyticsSource(val value: String) {
    @JsonProperty("telegram_bot")
    TELEGRAM_BOT("telegram_bot"),

    @JsonProperty("telegram_mini_app")
    TELEGRAM_MINI_APP("telegram_mini_app"),

    @JsonProperty("api")
    API("api");

    companion object {
        fun fromValue(value: String?): AnalyticsSource? =
            entries.firstOrNull { it.value == value }
    }
}
