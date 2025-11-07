package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.util.getUuidFromPath
import yayauheny.by.util.toPaginationRequest

class CityController(
    private val cityService: CityService
) {
    fun Route.cityRoutes() {
        route("/cities") {
            get {
                get("/cities") {
                    val pagination = call.toPaginationRequest()
                    val pageResponse = cityService.getAllCities(pagination)
                    call.respond(HttpStatusCode.OK, pageResponse)
                }
            }

            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val city =
                    cityService.getCityById(id)
                        ?: throw NotFoundException("City with id $id not found")
                call.respond(HttpStatusCode.OK, city)
            }

            get("/country/{countryId}") {
                val countryId = call.getUuidFromPath("countryId")
                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/search") {
                val name: String = call.parameters.getOrFail("name")
                val pagination = call.toPaginationRequest()
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

            delete("/delete") {
                val name: String = call.parameters.getOrFail("name")
                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.searchCitiesByName(name, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }
        }
    }
}
