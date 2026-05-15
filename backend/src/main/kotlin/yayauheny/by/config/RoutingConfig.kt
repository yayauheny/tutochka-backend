package yayauheny.by.config

import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.analytics.api.PublicAnalyticsController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.RestroomController
import yayauheny.by.importing.api.ImportController

fun Application.configureRouting() {
    val countryController by inject<CountryController>()
    val cityController by inject<CityController>()
    val restroomController by inject<RestroomController>()
    val analyticsController by inject<AnalyticsController>()
    val publicAnalyticsController by inject<PublicAnalyticsController>()
    val healthController by inject<HealthController>()
    val importController by inject<ImportController>()

    routing {
        with(publicAnalyticsController) { dashboardRoutes() }
        with(healthController) { healthRoutes() }

        route("/api/v1") {
            with(countryController) { countryRoutes() }
            with(cityController) { cityRoutes() }
            with(restroomController) { restroomRoutes() }
            with(importController) { importRoutes() }
            with(analyticsController) { analyticsRoutes() }
            with(publicAnalyticsController) { publicAnalyticsRoutes() }
        }
    }
}
