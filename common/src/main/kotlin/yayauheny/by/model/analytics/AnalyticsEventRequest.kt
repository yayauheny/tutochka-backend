package yayauheny.by.model.analytics







data class AnalyticsEventRequest(
    val event: ProductAnalyticsEvent,
    val lat: Double? = null,
    val lon: Double? = null,
    val resultsCount: Int? = null,
    val acquisitionTag: AcquisitionTag? = null,
    val metadata: Map<String, Any> = emptyMap()
)
