package yayauheny.by.common.plugins

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.SerializationException
import org.slf4j.LoggerFactory
import org.postgresql.util.PSQLException
import yayauheny.by.common.errors.ConflictException
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
                            status = cause.httpStatus.value,
                            message = cause.message,
                            path = call.request.path(),
                            errors = cause.errors
                        )
                    }
                    else -> {
                        ErrorResponse(
                            status = cause.httpStatus.value,
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
                    status = HttpStatusCode.BadRequest.value,
                    message = "Неверный формат JSON",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<BadRequestException> { call, cause ->
            logger.warn("Bad request exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = "Неверный формат запроса",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<ConflictException> { call, cause ->
            logger.warn("Conflict exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.Conflict.value,
                    message = cause.message ?: "Конфликт ресурсов",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.Conflict, errorResponse)
        }

        exception<PSQLException> { call, cause ->
            logger.warn("PostgreSQL exception occurred: ${cause.message}", cause)

            // Обработка ошибки уникальности (23505)
            val errorResponse =
                if (cause.sqlState == "23505") {
                    ErrorResponse(
                        status = HttpStatusCode.Conflict.value,
                        message = "Город с таким названием уже существует в этой стране",
                        path = call.request.path()
                    )
                } else {
                    ErrorResponse(
                        status = HttpStatusCode.InternalServerError.value,
                        message = "Ошибка базы данных",
                        path = call.request.path()
                    )
                }

            call.respond(
                if (cause.sqlState == "23505") HttpStatusCode.Conflict else HttpStatusCode.InternalServerError,
                errorResponse
            )
        }

        exception<IllegalArgumentException> { call, cause ->
            logger.warn("Illegal argument exception occurred: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.BadRequest.value,
                    message = cause.message ?: "Неверные параметры запроса",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.BadRequest, errorResponse)
        }

        exception<Throwable> { call, cause ->
            logger.error("Произошла неожиданная ошибка: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    message = "Внутренняя ошибка сервера",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }
    }
}
