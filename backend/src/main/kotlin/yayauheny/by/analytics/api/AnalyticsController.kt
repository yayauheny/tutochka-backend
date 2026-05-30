package yayauheny.by.analytics.api

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsEventRequest
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.common.util.toBigDecimalRounded

@RestController
@RequestMapping("/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {
    private val log = LoggerFactory.getLogger(AnalyticsController::class.java)

    @PostMapping("/events")
    fun trackEvent(
        @RequestBody request: AnalyticsEventRequest,
        @RequestHeader headers: HttpHeaders
    ): ResponseEntity<Void> {
        val identity = headers.getAnalyticsIdentityHeaders(AnalyticsSource.API)
        
        val command = AnalyticsEventCommand(
            event = request.event,
            tgUserId = identity.tgUserId,
            tgChatId = identity.tgChatId,
            username = identity.username,
            source = identity.source,
            lat = request.lat?.toBigDecimalRounded(),
            lon = request.lon?.toBigDecimalRounded(),
            resultsCount = request.resultsCount,
            durationMs = request.durationMs,
            acquisitionTag = request.acquisitionTag,
            metadata = request.metadata
        )

        runCatching {
            analyticsService.trackProductEvent(command)
        }.onFailure { exception ->
            log.warn("Failed to track analytics event", exception)
        }
        
        return ResponseEntity.accepted().build()
    }
}
