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
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.service.CityService
import yayauheny.by.service.validation.Validated
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.service.validation.cityUpdateValidator
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.service.validation.validateWith
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
                val name = call.request.queryParameters["name"]?.trim()
                val errors = mutableListOf<FieldError>()
                if (name.isNullOrBlank()) {
                    errors += FieldError("name", "Параметр name обязателен")
                } else if (name.length < 2) {
                    errors += FieldError("name", "Минимальная длина параметра name — 2 символа")
                }
                if (errors.isNotEmpty()) {
                    throw ValidationException(errors = errors)
                }

                val pagination = call.toPaginationRequest()
                val pageResponse = cityService.searchCitiesByName(name!!, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            post {
                val logger = LoggerFactory.getLogger("CityController")
                try {
                    val createDto = call.receive<CityCreateDto>()
                    logger.debug(
                        "POST /api/v1/cities - Received CityCreateDto: countryId=${createDto.countryId}, nameRu=${createDto.nameRu}, nameEn=${createDto.nameEn}, region=${createDto.region}, coordinates=(${createDto.coordinates.lat}, ${createDto.coordinates.lon})"
                    )

                    logger.debug("Validating CityCreateDto")
                    val validationResult = createDto.validateWith(cityCreateValidator)

                    when (validationResult) {
                        is Validated.Ok<CityCreateDto> -> {
                            logger.debug("Validation passed, calling cityService.createCity()")
                            val city = cityService.createCity(validationResult.value)
                            logger.info("City created successfully: id=${city.id}, nameRu=${city.nameRu}, nameEn=${city.nameEn}")
                            call.respond(HttpStatusCode.Created, city)
                        }
                        is Validated.Fail<CityCreateDto> -> {
                            logger.warn("Validation failed with ${validationResult.errors.size} error(s):")
                            validationResult.errors.forEach { error ->
                                logger.warn("  - Field '${error.field}': ${error.message}")
                            }
                            throw yayauheny.by.common.errors
                                .ValidationException(errors = validationResult.errors)
                        }
                    }
                } catch (e: yayauheny.by.common.errors.ValidationException) {
                    logger.debug("ValidationException caught, rethrowing: ${e.errors?.size} errors")
                    throw e
                } catch (e: Exception) {
                    logger.error("Unexpected error in POST /api/v1/cities", e)
                    throw e
                }
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
                when {
                    deleted -> call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                    else -> call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
