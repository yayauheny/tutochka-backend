package yayauheny.by.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry

class BackendSearchMetrics(
    private val registry: MeterRegistry
) {
    fun incrementSearchRequest(
        clientType: String,
        radiusBucket: String
    ) {
        Counter
            .builder("search_requests_total")
            .description("Total restroom search requests")
            .tag("client_type", MetricLabelWhitelist.clientTypeOrDefault(clientType))
            .tag("radius_bucket", radiusBucket)
            .register(registry)
            .increment()
    }

    fun incrementSearchResults(
        clientType: String,
        resultBucket: String
    ) {
        Counter
            .builder("search_results_total")
            .description("Search result distribution")
            .tag("client_type", MetricLabelWhitelist.clientTypeOrDefault(clientType))
            .tag("result_bucket", resultBucket)
            .register(registry)
            .increment()
    }

    fun incrementSearchQuality(
        clientType: String,
        qualityBucket: String
    ) {
        Counter
            .builder("search_quality_total")
            .description("Search quality distribution")
            .tag("client_type", MetricLabelWhitelist.clientTypeOrDefault(clientType))
            .tag("quality_bucket", qualityBucket)
            .register(registry)
            .increment()
    }

    fun incrementSearchFailure(reason: String) {
        Counter
            .builder("search_failures_total")
            .description("Search failures")
            .tag("reason", MetricLabelWhitelist.failureReasonOrDefault(reason))
            .register(registry)
            .increment()
    }
}
