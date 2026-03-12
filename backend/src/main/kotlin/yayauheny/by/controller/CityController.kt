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
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.CitySearchParams
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateCityOnUpdate
import yayauheny.by.service.validation.validateCitySearchParams
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateRegion
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
                        ?: throw NotFoundException("Город с ID '$id' не найден")
                call.respond(HttpStatusCode.OK, city)
            }

            get("/country/{countryId}") {
                val countryId = call.getUuidFromPath("countryId")
                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.getCitiesByCountry(countryId, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/search") {
                val nameParam = call.request.queryParameters["name"]?.trim()
                val searchParams = CitySearchParams(name = nameParam ?: "")
                val validParams = searchParams.validateAndThen(validateCitySearchParams) { it }.getOrThrow()

                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.searchCitiesByName(validParams.name, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            post {
                val createDto = call.receive<CityCreateDto>()
                createDto.validateOrThrow(validateCityOnCreate)
                val additionalErrors = validateRegion(createDto.region)
                if (additionalErrors.isNotEmpty()) {
                    throw ValidationException(additionalErrors)
                }
                val city = cityService.createCity(createDto)
                call.respond(HttpStatusCode.Created, city)
            }

            put("/{id}") {
                val id = call.getUuidFromPath("id")
                val updateDto = call.receive<CityUpdateDto>()
                updateDto.validateOrThrow(validateCityOnUpdate)
                val additionalErrors = validateRegion(updateDto.region)
                if (additionalErrors.isNotEmpty()) {
                    throw ValidationException(additionalErrors)
                }
                val city = cityService.updateCity(id, updateDto)
                call.respond(HttpStatusCode.OK, city)
            }

            delete("/{id}") {
                val id = call.getUuidFromPath("id")
                val deleted = cityService.deleteCity(id)
                when {
                    deleted -> call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                    else -> call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
