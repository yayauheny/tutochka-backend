package yayauheny.by.analytics.service

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import yayauheny.by.analytics.model.AnalyticsTrackRequestedEvent

@Component
class BusinessAnalyticsEventListener(
    private val analyticsService: AnalyticsService,
    private val businessAnalyticsMetrics: BusinessAnalyticsMetrics
) {
    private val log = LoggerFactory.getLogger(BusinessAnalyticsEventListener::class.java)

    @Async("analyticsTaskExecutor")
    @EventListener
    fun handle(event: AnalyticsTrackRequestedEvent) {
        val command = event.command
        runCatching {
            analyticsService.trackProductEvent(command)
        }.onSuccess { tracked ->
            if (tracked) {
                businessAnalyticsMetrics.incrementRecorded(command.event, command.source.value)
            } else {
                businessAnalyticsMetrics.incrementFailure(command.event, "not_trackable")
            }
        }.onFailure { exception ->
            businessAnalyticsMetrics.incrementFailure(command.event, "persistence")
            log.warn("Failed to record business analytics event: event={}", command.event.value, exception)
        }
    }
}
