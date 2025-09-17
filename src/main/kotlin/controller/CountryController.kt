package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import yayauheny.by.model.CountryCreateDto
import yayauheny.by.service.CountryService
import yayauheny.by.util.createPaginationFromQuery
import yayauheny.by.util.getUuidFromPath
import yayauheny.by.util.getStringFromPath

class CountryController(private val countryService: CountryService) {
    
    fun Route.countryRoutes() {
        route("/countries") {
            get {
                val pagination = call.createPaginationFromQuery()
                val pageResponse = countryService.getAllCountries(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }
            
            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val country = countryService.getCountryById(id)
                country?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            
            get("/code/{code}") {
                val code = call.getStringFromPath("code")
                val country = countryService.getCountryByCode(code)
                country?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.NotFound)
            }
            
            post {
                val createDto = call.receive<CountryCreateDto>()
                val country = countryService.createCountry(createDto)
                call.respond(HttpStatusCode.Created, country)
            }
        }
    }
}
