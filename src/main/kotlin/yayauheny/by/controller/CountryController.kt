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
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.service.CountryService
import yayauheny.by.service.validation.validateCountryOnCreate
import yayauheny.by.service.validation.validateCountryOnUpdate
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.util.getStringFromPath
import yayauheny.by.util.getUuidFromPath
import yayauheny.by.util.toPaginationRequest

class CountryController(
    private val countryService: CountryService
) {
    fun Route.countryRoutes() {
        route("/countries") {
            get {
                val pagination = call.toPaginationRequest()
                val pageResponse = countryService.getAllCountries(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val country =
                    countryService.getCountryById(id)
                        ?: throw NotFoundException("Страна с ID '$id' не найдена")
                call.respond(HttpStatusCode.OK, country)
            }

            get("/code/{code}") {
                val code = call.getStringFromPath("code")
                val country =
                    countryService.getCountryByCode(code)
                        ?: throw NotFoundException("Страна с кодом '$code' не найдена")
                call.respond(HttpStatusCode.OK, country)
            }

            post {
                val createDto = call.receive<CountryCreateDto>()
                createDto.validateOrThrow(validateCountryOnCreate)
                val country = countryService.createCountry(createDto)
                call.respond(HttpStatusCode.Created, country)
            }

            put("/{id}") {
                val id = call.getUuidFromPath("id")
                val updateDto = call.receive<yayauheny.by.model.country.CountryUpdateDto>()
                updateDto.validateOrThrow(validateCountryOnUpdate)
                val country = countryService.updateCountry(id, updateDto)
                call.respond(HttpStatusCode.OK, country)
            }

            delete("/{id}") {
                val id = call.getUuidFromPath("id")
                val deleted = countryService.deleteCountry(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
