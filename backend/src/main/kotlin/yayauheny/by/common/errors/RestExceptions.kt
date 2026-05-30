package yayauheny.by.common.errors

import org.springframework.http.HttpStatus

interface RestException {
    val httpStatus: HttpStatus
}

class ValidationException(
    val errors: List<FieldError>? = null,
    message: String? = "Ошибка валидации",
    cause: Throwable? = null
) : Exception(message ?: errors?.joinToString { "${it.field}: ${it.message}" }, cause), RestException {
    override val httpStatus = HttpStatus.BAD_REQUEST

    constructor(error: FieldError) : this(listOf(error))
}

class NotFoundException(
    message: String? = "Ресурс не найден",
    cause: Throwable? = null
) : Exception(message, cause), RestException {
    override val httpStatus = HttpStatus.NOT_FOUND
}

class ConflictException(
    message: String? = "Конфликт ресурсов",
    cause: Throwable? = null
) : Exception(message, cause), RestException {
    override val httpStatus = HttpStatus.CONFLICT
}

class BadRequestException(
    message: String? = "Неверный запрос",
    cause: Throwable? = null
) : Exception(message, cause), RestException {
    override val httpStatus = HttpStatus.BAD_REQUEST
}

class ServiceUnavailableException(
    message: String? = "Сервис временно недоступен",
    cause: Throwable? = null,
    val retryAfter: Int? = null
) : Exception(message, cause), RestException {
    override val httpStatus = HttpStatus.SERVICE_UNAVAILABLE
}

class RepositoryException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

class EntityNotFoundException(
    entityType: String,
    entityId: String? = null,
    cause: Throwable? = null
) : Exception(
        entityId?.let { "$entityType с ID $it не найден" } ?: "$entityType не найден",
        cause
    )
