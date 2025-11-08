package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.NotFoundException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.service.CountryService
import yayauheny.by.service.validation.countryCreateValidator
import yayauheny.by.service.validation.countryUpdateValidator
import yayauheny.by.service.validation.validateAndThen
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
                        ?: throw NotFoundException("Country with id $id not found")
                call.respond(HttpStatusCode.OK, country)
            }

            get("/code/{code}") {
                val code = call.getStringFromPath("code")
                val country =
                    countryService.getCountryByCode(code)
                        ?: throw NotFoundException("Country with code $code not found")
                call.respond(HttpStatusCode.OK, country)
            }

            post {
                val createDto = call.receive<CountryCreateDto>()
                val country =
                    createDto.validateAndThen(countryCreateValidator) { valid ->
                        countryService.createCountry(valid)
                    }
                call.respond(HttpStatusCode.Created, country)
            }

            put("/{id}") {
                val id = call.getUuidFromPath("id")
                val updateDto = call.receive<yayauheny.by.model.country.CountryUpdateDto>()
                val country =
                    updateDto.validateAndThen(countryUpdateValidator) { valid ->
                        countryService.updateCountry(id, valid)
                    }
                if (country != null) {
                    call.respond(HttpStatusCode.OK, country)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
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
