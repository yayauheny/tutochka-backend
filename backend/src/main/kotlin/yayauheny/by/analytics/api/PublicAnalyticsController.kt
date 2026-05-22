package yayauheny.by.analytics.api

import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import yayauheny.by.analytics.service.PublicAnalyticsService

class PublicAnalyticsController(
    private val publicAnalyticsService: PublicAnalyticsService
) {
    fun Route.publicAnalyticsRoutes() {
        route("/public") {
            get("/analytics") {
                call.respond(publicAnalyticsService.getPublicAnalytics())
            }
        }
    }
}
