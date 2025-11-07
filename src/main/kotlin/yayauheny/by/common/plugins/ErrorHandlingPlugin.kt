package yayauheny.by.common.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import java.time.Instant
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.ErrorResponse
import yayauheny.by.common.errors.RestException
import yayauheny.by.common.errors.ValidationException

private val logger = LoggerFactory.getLogger("ErrorHandling")

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<RestException> { call, cause ->
            logger.warn("REST exception occurred: ${cause.message}", cause)

            val errorResponse =
                when (cause) {
                    is ValidationException -> {
                        ErrorResponse(
                            timestamp = Instant.now().toString(),
                            status = cause.httpStatus.value,
                            error = cause.httpStatus.description,
                            message = cause.message,
                            path = call.request.path(),
                            errors = cause.errors
                        )
                    }
                    else -> {
                        ErrorResponse(
                            timestamp = Instant.now().toString(),
                            status = cause.httpStatus.value,
                            error = cause.httpStatus.description,
                            message = cause.message,
                            path = call.request.path()
                        )
                    }
                }

            call.respond(cause.httpStatus, errorResponse)
        }

        exception<SerializationException> { call, cause ->
            logger.warn("Serialization exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = "Неверный формат JSON",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<BadRequestException> { call, cause ->
            logger.warn("Bad request exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = "Неверный формат запроса",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Illegal argument exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.BadRequest.value,
                    error = HttpStatusCode.BadRequest.description,
                    message = cause.message ?: "Неверные параметры запроса",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<Throwable> { call, cause ->
            logger.error("Unexpected exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    timestamp = Instant.now().toString(),
                    status = HttpStatusCode.InternalServerError.value,
                    error = HttpStatusCode.InternalServerError.description,
                    message = "Внутренняя ошибка сервера",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }
}
