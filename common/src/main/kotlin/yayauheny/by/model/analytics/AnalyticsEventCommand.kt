package yayauheny.by.model.analytics

data class AnalyticsEventCommand(
    val event: ProductAnalyticsEvent,
    val tgUserId: Long?,
    val tgChatId: String?,
    val username: String?,
    val source: AnalyticsSource = AnalyticsSource.TELEGRAM_BOT,
    val lat: Double? = null,
    val lon: Double? = null,
    val resultsCount: Int? = null,
    val acquisitionTag: AcquisitionTag? = null,
    val metadata: Map<String, Any> = emptyMap()
)
