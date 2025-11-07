package yayauheny.by.config

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.RestroomController

fun Application.configureRouting() {
    val countryController by inject<CountryController>()
    val cityController by inject<CityController>()
    val restroomController by inject<RestroomController>()

    routing {
        get("/health") {
            call.response.headers.append("Content-Type", "application/json")
            call.respond(HttpStatusCode.OK, mapOf("status" to "healthy", "yayauheny/by/serviceny/by/service" to "TuTochka API"))
        }

        route("/api/v1") {
            with(countryController) { countryRoutes() }
            with(cityController) { cityRoutes() }
            with(restroomController) { restroomRoutes() }
        }
    }
}
