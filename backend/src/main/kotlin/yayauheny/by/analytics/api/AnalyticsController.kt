package yayauheny.by.analytics.api

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsEventRequest
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.common.util.toBigDecimalRounded

@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val analyticsService: AnalyticsService
) {
    private val log = LoggerFactory.getLogger(AnalyticsController::class.java)

    @PostMapping("/events")
    fun trackEvent(
        @RequestBody request: AnalyticsEventRequest,
        @RequestHeader(name = "X-Tg-User-Id", required = false) tgUserId: Long?,
        @RequestHeader(name = "X-Tg-Chat-Id", required = false) tgChatId: String?,
        @RequestHeader(name = "X-Tg-Username", required = false) username: String?,
        @RequestHeader(name = "X-Analytics-Source", required = false) sourceHeader: String?
    ): ResponseEntity<Void> {
        val source = sourceHeader?.let { AnalyticsSource.fromValue(it) } ?: AnalyticsSource.API
        
        val command = AnalyticsEventCommand(
            event = request.event,
            tgUserId = tgUserId,
            tgChatId = tgChatId,
            username = username,
            source = source,
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
