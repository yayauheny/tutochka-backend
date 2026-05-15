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
import yayauheny.by.common.errors.ApiError
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.errors.ErrorResponse
import yayauheny.by.common.errors.RepositoryException
import yayauheny.by.common.errors.RestException
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.importing.exception.CityNotFound
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.exception.UnsupportedImportProvider
import yayauheny.by.importing.exception.UnsupportedPayloadType

private val logger = LoggerFactory.getLogger("ErrorHandling")

fun Application.configureErrorHandling() {
    install(StatusPages) {
        exception<UnsupportedImportProvider> { call, cause ->
            logger.warn("Import: unsupported provider: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ApiError("UNSUPPORTED_PROVIDER", cause.message ?: ""))
        }

        exception<UnsupportedPayloadType> { call, cause ->
            logger.warn("Import: unsupported payload type: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ApiError("UNSUPPORTED_PAYLOAD_TYPE", cause.message ?: ""))
        }

        exception<CityNotFound> { call, cause ->
            logger.warn("Import: city not found: ${cause.message}")
            call.respond(HttpStatusCode.NotFound, ApiError("CITY_NOT_FOUND", cause.message ?: ""))
        }

        exception<InvalidImportPayload> { call, cause ->
            logger.warn("Import: invalid payload: ${cause.message}")
            call.respond(HttpStatusCode.BadRequest, ApiError("INVALID_PAYLOAD", cause.message ?: ""))
        }

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

        exception<EntityNotFoundException> { call, cause ->
            logger.warn("Entity not found: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.NotFound.value,
                    message = cause.message,
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.NotFound, errorResponse)
        }

        exception<RepositoryException> { call, cause ->
            logger.error("Repository error: ${cause.message}", cause)

            val errorResponse =
                ErrorResponse(
                    status = HttpStatusCode.InternalServerError.value,
                    message = "Ошибка доступа к данным",
                    path = call.request.path()
                )

            call.respond(HttpStatusCode.InternalServerError, errorResponse)
        }

        exception<PSQLException> { call, cause ->
            logger.warn("PostgreSQL exception occurred: ${cause.message}", cause)

            val (statusCode, message) =
                when (cause.sqlState) {
                    "23505" -> HttpStatusCode.Conflict to "Нарушение уникальности: запись с такими данными уже существует"
                    "23503" -> HttpStatusCode.BadRequest to "Нарушение внешнего ключа: связанная запись не найдена"
                    "23502" -> HttpStatusCode.BadRequest to "Нарушение ограничения: обязательное поле не может быть пустым"
                    "23514" -> HttpStatusCode.BadRequest to "Нарушение ограничения проверки: данные не соответствуют требованиям"
                    "42P01" -> HttpStatusCode.InternalServerError to "Ошибка базы данных: таблица не найдена"
                    "08000", "08003", "08006", "08001", "08004", "08007", "08P01" ->
                        HttpStatusCode.ServiceUnavailable to
                            "Ошибка подключения к базе данных"
                    else -> HttpStatusCode.InternalServerError to "Ошибка базы данных"
                }

            val errorResponse =
                ErrorResponse(
                    status = statusCode.value,
                    message = message,
                    path = call.request.path()
                )

            call.respond(statusCode, errorResponse)
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
