package yayauheny.by.analytics.service

import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.model.AnalyticsTrackRequestedEvent
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@Service
class BusinessAnalyticsPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val businessAnalyticsMetrics: BusinessAnalyticsMetrics
) {
    private val log = LoggerFactory.getLogger(BusinessAnalyticsPublisher::class.java)

    fun trackProductEvent(command: AnalyticsEventCommand) {
        runCatching {
            applicationEventPublisher.publishEvent(AnalyticsTrackRequestedEvent(command))
        }.onFailure { exception ->
            businessAnalyticsMetrics.incrementFailure(command.event, "dispatch")
            log.warn("Failed to dispatch business analytics event: event={}", command.event.value, exception)
        }
    }

    fun trackNearestRestroomsSearch(
        identity: AnalyticsIdentityHeaders,
        resultsCount: Int,
        lat: Double,
        lon: Double
    ) {
        val event =
            if (resultsCount > 0) {
                ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED
            } else {
                ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS
            }

        trackProductEvent(
            AnalyticsEventCommand(
                event = event,
                tgUserId = identity.tgUserId,
                tgChatId = identity.tgChatId,
                username = identity.username,
                source = identity.source,
                lat = lat,
                lon = lon,
                resultsCount = resultsCount
            )
        )
    }
}
