package yayauheny.by.model.analytics

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class AnalyticsSource(val value: String) {
    @SerialName("telegram_bot")
    TELEGRAM_BOT("telegram_bot"),

    @SerialName("telegram_mini_app")
    TELEGRAM_MINI_APP("telegram_mini_app"),

    @SerialName("api")
    API("api");

    companion object {
        fun fromValue(value: String?): AnalyticsSource? =
            entries.firstOrNull { it.value == value }
    }
}
