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
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.service.RestroomService
import yayauheny.by.service.validation.NearestRestroomsParams
import yayauheny.by.service.validation.validateAndThen
import yayauheny.by.service.validation.validateNearestRestroomsParams
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateRestroomCreateFields
import yayauheny.by.service.validation.validateRestroomOnCreate
import yayauheny.by.service.validation.validateRestroomOnUpdate
import yayauheny.by.service.validation.validateRestroomUpdateFields
import yayauheny.by.util.createPaginationFromQuery
import yayauheny.by.util.getDoubleFromQuery
import yayauheny.by.util.getIntFromQuery
import yayauheny.by.util.getUuidFromPath
import yayauheny.by.util.toPaginationRequest

class RestroomController(
    private val restroomService: RestroomService
) {
    fun Route.restroomRoutes() {
        route("/restrooms") {
            get {
                val pagination = call.toPaginationRequest()
                val pageResponse = restroomService.getAllRestrooms(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val restroom =
                    restroomService.getRestroomById(id)
                        ?: throw NotFoundException("Туалет с ID '$id' не найден")
                call.respond(HttpStatusCode.OK, restroom)
            }

            get("/city/{cityId}") {
                val cityId = call.getUuidFromPath("cityId")
                val pagination = call.createPaginationFromQuery()
                val pageResponse = restroomService.getRestroomsByCity(cityId, pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/nearest") {
                val lat = call.getDoubleFromQuery("lat")
                val lon = call.getDoubleFromQuery("lon")
                val limit = call.getIntFromQuery("limit") ?: 5
                val distanceMeters = call.getIntFromQuery("distanceMeters") ?: ApiConstants.DEFAULT_MAX_DISTANCE_METERS
                val params =
                    NearestRestroomsParams(
                        Coordinates(lat, lon),
                        limit,
                        distanceMeters
                    )

                val restrooms =
                    params
                        .validateAndThen(validateNearestRestroomsParams) { valid ->
                            restroomService.findNearestRestrooms(
                                valid.coordinates.lat,
                                valid.coordinates.lon,
                                valid.limit,
                                valid.distanceMeters
                            )
                        }.getOrThrow()

                call.respond(HttpStatusCode.OK, restrooms)
            }

            post {
                val createDto = call.receive<RestroomCreateDto>()
                createDto.validateOrThrow(validateRestroomOnCreate)
                val additionalErrors = validateRestroomCreateFields(createDto)
                if (additionalErrors.isNotEmpty()) {
                    throw ValidationException(additionalErrors)
                }
                val restroom = restroomService.createRestroom(createDto)
                call.respond(HttpStatusCode.Created, restroom)
            }

            put("/{id}") {
                val id = call.getUuidFromPath("id")
                val updateDto = call.receive<RestroomUpdateDto>()
                updateDto.validateOrThrow(validateRestroomOnUpdate)
                val additionalErrors = validateRestroomUpdateFields(updateDto)
                if (additionalErrors.isNotEmpty()) {
                    throw ValidationException(additionalErrors)
                }
                val restroom = restroomService.updateRestroom(id, updateDto)
                call.respond(HttpStatusCode.OK, restroom)
            }

            delete("/{id}") {
                val id = call.getUuidFromPath("id")
                val deleted = restroomService.deleteRestroom(id)
                if (deleted) {
                    call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
