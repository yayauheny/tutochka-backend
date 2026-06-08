package yayauheny.by.analytics.service

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Component
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@Component
class BusinessAnalyticsMetrics(
    private val registry: MeterRegistry
) {
    fun incrementRecorded(
        event: ProductAnalyticsEvent,
        source: String
    ) {
        Counter
            .builder("business_analytics_events_total")
            .description("Recorded business analytics events")
            .tag("event", event.value)
            .tag("source", source)
            .register(registry)
            .increment()
    }

    fun incrementFailure(
        event: ProductAnalyticsEvent,
        reason: String
    ) {
        Counter
            .builder("business_analytics_failures_total")
            .description("Business analytics events that were not recorded")
            .tag("event", event.value)
            .tag("reason", reason)
            .register(registry)
            .increment()
    }
}
