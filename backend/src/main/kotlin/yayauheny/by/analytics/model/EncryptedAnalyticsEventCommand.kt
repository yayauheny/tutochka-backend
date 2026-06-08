package yayauheny.by.analytics.model

import yayauheny.by.model.analytics.AcquisitionTag
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

data class EncryptedAnalyticsEventCommand(
    val event: ProductAnalyticsEvent,
    val tgUserId: String?,
    val tgChatId: String?,
    val username: String?,
    val source: AnalyticsSource = AnalyticsSource.TELEGRAM_BOT,
    val lat: Double? = null,
    val lon: Double? = null,
    val resultsCount: Int? = null,
    val acquisitionTag: AcquisitionTag? = null,
    val metadata: Map<String, Any> = emptyMap()
)
