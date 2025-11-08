package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.util.getOrFail
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.service.validation.cityUpdateValidator
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.util.getUuidFromPath
import yayauheny.by.util.toPaginationRequest

class CityController(
    private val cityService: CityService
) {
    fun Route.cityRoutes() {
        route("/cities") {
            get {
                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.getAllCities(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
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

            put("/{id}") {
                val id = call.getUuidFromPath("id")
                val updateDto = call.receive<CityUpdateDto>()
                val city =
                    updateDto.validateAndThen(cityUpdateValidator) { valid ->
                        cityService.updateCity(id, valid)
                    }
                if (city != null) {
                    call.respond(HttpStatusCode.OK, city)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            delete("/{id}") {
                val id = call.getUuidFromPath("id")
                val deleted = cityService.deleteCity(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
