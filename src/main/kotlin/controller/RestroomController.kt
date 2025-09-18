package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.service.RestroomService
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
                val restroom = restroomService.getRestroomById(id)
                restroom?.let {
                    call.respond(HttpStatusCode.OK, it)
                } ?: call.respond(HttpStatusCode.NotFound)
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
                val limit = call.getIntFromQuery("limit", 5)

                val restrooms = restroomService.findNearestRestrooms(lat, lon, limit)
                call.respond(HttpStatusCode.OK, restrooms)
            }

            post {
                val createDto = call.receive<RestroomCreateDto>()
                val restroom = restroomService.createRestroom(createDto)
                call.respond(HttpStatusCode.Created, restroom)
            }
        }
    }
}
