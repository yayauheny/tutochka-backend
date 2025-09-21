package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import yayauheny.by.model.CityCreateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.cityCreateValidator
import service.validation.validateAndThen
import yayauheny.by.util.createPaginationFromQuery
import yayauheny.by.util.getUuidFromPath

class CityController(
    private val cityService: CityService
) {
    fun Route.cityRoutes() {
        route("/cities") {
            get {
                val pagination = call.createPaginationFromQuery()
                val pageResponse = cityService.getAllCities(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val city =
                    cityService.getCityById(id)
                        ?: throw yayauheny.by.common.errors
                            .NotFoundException("City with id $id not found")
                call.respond(HttpStatusCode.OK, city)
            }

            get("/country/{countryId}") {
                val countryId = call.getUuidFromPath("countryId")
                val pagination = call.createPaginationFromQuery()
                val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/search") {
                val name =
                    call.request.queryParameters["name"]
                        ?: throw yayauheny.by.common.errors
                            .ValidationException("Missing required parameter 'name'")
                val pagination = call.createPaginationFromQuery()
                val pageResponse = cityService.searchCitiesByName(name, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            post {
                val createDto = call.receive<CityCreateDto>()
                val city =
                    createDto.validateAndThen(cityCreateValidator) { valid ->
                        cityService.createCity(valid)
                    }
                call.respond(HttpStatusCode.Created, city)
            }
        }
    }
}
