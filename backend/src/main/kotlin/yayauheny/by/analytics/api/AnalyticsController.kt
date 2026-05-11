package yayauheny.by.analytics.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsEventRequest
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.util.toBigDecimalRounded

class AnalyticsController(
    private val analyticsService: AnalyticsService
) {
    private val log = LoggerFactory.getLogger(AnalyticsController::class.java)

    fun Route.analyticsRoutes() {
        route("/analytics") {
            post("/events") {
                call.trackEvent()
            }
        }
    }

    private suspend fun ApplicationCall.trackEvent() {
        val request = receive<AnalyticsEventRequest>()
        val identity = getAnalyticsIdentityHeaders(AnalyticsSource.API)
        val command =
            AnalyticsEventCommand(
                event = request.event,
                tgUserId = identity.tgUserId,
                tgChatId = identity.tgChatId,
                username = identity.username,
                source = identity.source,
                lat = request.lat?.toBigDecimalRounded(),
                lon = request.lon?.toBigDecimalRounded(),
                resultsCount = request.resultsCount,
                durationMs = request.durationMs,
                metadata = request.metadata
            )

        runCatching {
            analyticsService.trackProductEvent(command)
        }.onFailure { exception ->
            log.warn("Failed to track analytics event", exception)
        }

        respond(HttpStatusCode.Accepted)
    }
}
