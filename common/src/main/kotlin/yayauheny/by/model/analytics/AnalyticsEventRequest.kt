package yayauheny.by.model.analytics

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class AnalyticsEventRequest(
    val event: ProductAnalyticsEvent,
    val lat: Double? = null,
    val lon: Double? = null,
    val resultsCount: Int? = null,
    val durationMs: Int? = null,
    val metadata: JsonObject = JsonObject(emptyMap())
)
