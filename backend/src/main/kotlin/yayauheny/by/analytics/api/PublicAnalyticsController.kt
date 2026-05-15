package yayauheny.by.analytics.api

import io.ktor.http.ContentType
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import yayauheny.by.analytics.service.PublicAnalyticsService

class PublicAnalyticsController(
    private val publicAnalyticsService: PublicAnalyticsService
) {
    private val dashboardHtml = loadDashboardHtml()

    fun Route.dashboardRoutes() {
        get("/") {
            call.respondText(dashboardHtml, ContentType.Text.Html)
        }
    }

    fun Route.publicAnalyticsRoutes() {
        route("/public") {
            get("/analytics") {
                call.respond(publicAnalyticsService.getPublicAnalytics())
            }
        }
    }

    private fun loadDashboardHtml(): String =
        this::class.java.classLoader
            .getResourceAsStream("public-analytics.html")
            ?.bufferedReader()
            ?.use { it.readText() }
            ?: error("public-analytics.html resource is missing")
}
