package yayauheny.by.config

import io.ktor.server.application.Application
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
        route("/api/v1") {
            with(countryController) { countryRoutes() }
            with(cityController) { cityRoutes() }
            with(restroomController) { restroomRoutes() }
        }
    }
}
