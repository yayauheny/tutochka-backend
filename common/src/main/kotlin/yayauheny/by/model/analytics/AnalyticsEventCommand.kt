package yayauheny.by.model.analytics



import java.math.BigDecimal


data class AnalyticsEventCommand(
    val event: ProductAnalyticsEvent,
    val tgUserId: Long?,
    val tgChatId: String?,
    val username: String?,
    val source: AnalyticsSource = AnalyticsSource.TELEGRAM_BOT,
    val lat: BigDecimal? = null,
    val lon: BigDecimal? = null,
    val resultsCount: Int? = null,
    val durationMs: Int? = null,
    val acquisitionTag: AcquisitionTag? = null,
    val metadata: Map<String, Any> = emptyMap()
)
