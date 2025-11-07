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
        message: String? = "Validation failed",
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
    message: String? = "Resource not found",
    cause: Throwable? = null
) : RestException(HttpStatusCode.NotFound, message, cause)

class ConflictException(
    message: String? = "Resource conflict",
    cause: Throwable? = null
) : RestException(HttpStatusCode.Conflict, message, cause)
