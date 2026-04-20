package yayauheny.by.metrics

object MetricLabelWhitelist {
    private val clientTypes = setOf("telegram_bot", "telegram_miniapp", "api")
    private val failureReasons = setOf("validation", "not_found", "internal")

    fun clientTypeOrDefault(value: String?): String {
        val normalized = value?.trim()?.lowercase(java.util.Locale.ROOT).orEmpty()
        return if (normalized in clientTypes) normalized else "api"
    }

    fun failureReasonOrDefault(value: String?): String {
        val normalized = value?.trim()?.lowercase(java.util.Locale.ROOT).orEmpty()
        return if (normalized in failureReasons) normalized else "internal"
    }
}
