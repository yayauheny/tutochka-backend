package yayauheny.by.analytics.api

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsEventRequest
import yayauheny.by.model.analytics.AnalyticsSource

@RestController
@RequestMapping("/api/v1/analytics")
class AnalyticsController(
    private val businessAnalyticsPublisher: BusinessAnalyticsPublisher
) {
    @PostMapping("/events")
    fun trackEvent(
        @RequestBody request: AnalyticsEventRequest,
        @RequestHeader(name = "X-Tg-User-Id", required = false) tgUserId: Long?,
        @RequestHeader(name = "X-Tg-Chat-Id", required = false) tgChatId: String?,
        @RequestHeader(name = "X-Tg-Username", required = false) username: String?,
        @RequestHeader(name = "X-Analytics-Source", required = false) sourceHeader: String?
    ): ResponseEntity<Void> {
        val source = sourceHeader?.let { AnalyticsSource.fromValue(it) } ?: AnalyticsSource.API
        val normalizedUsername = username?.trim()?.takeIf { it.isNotEmpty() }
        
        val command = AnalyticsEventCommand(
            event = request.event,
            tgUserId = tgUserId,
            tgChatId = tgChatId,
            username = normalizedUsername,
            source = source,
            lat = request.lat,
            lon = request.lon,
            resultsCount = request.resultsCount,
            acquisitionTag = request.acquisitionTag,
            metadata = request.metadata
        )

        businessAnalyticsPublisher.trackProductEvent(command)

        return ResponseEntity.accepted().build()
    }
}
