package yayauheny.by.analytics.model

import java.math.BigDecimal
import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

data class EncryptedAnalyticsEventCommand(
    val event: ProductAnalyticsEvent,
    val tgUserId: String?,
    val tgChatId: String?,
    val username: String?,
    val source: AnalyticsSource = AnalyticsSource.TELEGRAM_BOT,
    val lat: BigDecimal? = null,
    val lon: BigDecimal? = null,
    val resultsCount: Int? = null,
    val durationMs: Int? = null,
    val metadata: JsonObject = JsonObject(emptyMap())
)
