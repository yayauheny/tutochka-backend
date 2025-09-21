package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import service.validation.validateAndThen
import yayauheny.by.common.errors.NotFoundException
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.service.RestroomService
import yayauheny.by.service.validation.NearestRestroomsParams
import yayauheny.by.service.validation.nearestRestroomsParamsValidator
import yayauheny.by.service.validation.restroomCreateValidator
import yayauheny.by.util.createPaginationFromQuery
import yayauheny.by.util.getDoubleFromQuery
import yayauheny.by.util.getIntFromQuery
import yayauheny.by.util.getUuidFromPath

class RestroomController(
    private val restroomService: RestroomService
) {
    fun Route.restroomRoutes() {
        route("/restrooms") {
            get {
                val pagination = call.createPaginationFromQuery()
                val pageResponse = restroomService.getAllRestrooms(pagination)
                call.respond(HttpStatusCode.OK, pageResponse)
            }

            get("/{id}") {
                val id = call.getUuidFromPath("id")
                val restroom =
                    restroomService.getRestroomById(id)
                        ?: throw NotFoundException("Restroom with id $id not found")
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

                val params = NearestRestroomsParams(lat, lon, limit)
                val restrooms =
                    params.validateAndThen(nearestRestroomsParamsValidator) { valid ->
                        restroomService.findNearestRestrooms(valid.lat, valid.lon, valid.limit)
                    }
                call.respond(HttpStatusCode.OK, restrooms)
            }

            post {
                val createDto = call.receive<RestroomCreateDto>()
                val restroom =
                    createDto.validateAndThen(restroomCreateValidator) { valid ->
                        restroomService.createRestroom(valid)
                    }
                call.respond(HttpStatusCode.Created, restroom)
            }
        }
    }
}
