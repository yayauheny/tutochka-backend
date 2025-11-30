package yayauheny.by.common.errors

import io.ktor.http.HttpStatusCode

abstract class RestException(
    val httpStatus: HttpStatusCode,
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)

class ValidationException : RestException {
    val errors: List<FieldError>?

    constructor(
        message: String? = "Ошибка валидации",
        cause: Throwable? = null
    ) : super(HttpStatusCode.BadRequest, message, cause) {
        this.errors = null
    }

    constructor(errors: List<FieldError>) : super(
        HttpStatusCode.BadRequest,
        errors.joinToString { "${it.field}: ${it.message}" }
    ) {
        this.errors = errors
    }
}

class NotFoundException(
    message: String? = "Ресурс не найден",
    cause: Throwable? = null
) : RestException(HttpStatusCode.NotFound, message, cause)

class ConflictException(
    message: String? = "Конфликт ресурсов",
    cause: Throwable? = null
) : RestException(HttpStatusCode.Conflict, message, cause)

class BadRequestException(
    message: String? = "Неверный запрос",
    cause: Throwable? = null
) : RestException(HttpStatusCode.BadRequest, message, cause)

class ServiceUnavailableException(
    message: String? = "Сервис временно недоступен",
    cause: Throwable? = null,
    val retryAfter: Int? = null
) : RestException(HttpStatusCode.ServiceUnavailable, message, cause)

/**
 * Базовое исключение для ошибок репозитория.
 * Используется для внутренних ошибок доступа к данным.
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Исключение, выбрасываемое когда сущность не найдена в репозитории.
 * Автоматически преобразуется в NotFoundException на уровне обработки ошибок.
 */
class EntityNotFoundException(
    entityType: String,
    entityId: String? = null,
    cause: Throwable? = null
) : Exception(
        entityId?.let { "$entityType с ID $it не найден" } ?: "$entityType не найден",
        cause
    )
