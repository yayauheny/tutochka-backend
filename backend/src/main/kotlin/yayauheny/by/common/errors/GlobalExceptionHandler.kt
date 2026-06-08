package yayauheny.by.common.errors

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.servlet.resource.NoResourceFoundException
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.ConstraintViolationException
import org.springframework.http.converter.HttpMessageNotReadableException
import java.lang.reflect.UndeclaredThrowableException
import yayauheny.by.importing.exception.ImportException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(
        ValidationException::class, 
        NotFoundException::class, 
        ConflictException::class, 
        BadRequestException::class, 
        ServiceUnavailableException::class,
        NotImplementedException::class
    )
    fun handleRestException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val restException = e as RestException
        val status = restException.httpStatus
        
        var fieldErrors: List<FieldError>? = null
        if (e is ValidationException) {
            fieldErrors = e.errors
        }

        val response = ErrorResponse(
            status = status.value(),
            message = e.message,
            path = request.requestURI,
            errors = fieldErrors
        )
        val builder = ResponseEntity.status(status)
        if (e is ServiceUnavailableException && e.retryAfter != null) {
            builder.header(HttpHeaders.RETRY_AFTER, e.retryAfter.toString())
        }
        return builder.body(response)
    }

    @ExceptionHandler(EntityNotFoundException::class)
    fun handleEntityNotFoundException(e: EntityNotFoundException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = e.message,
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(e: MethodArgumentNotValidException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val fieldErrors = e.bindingResult.fieldErrors.map {
            FieldError(
                field = it.field,
                message = it.defaultMessage ?: "Invalid value"
            )
        }
        
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "Validation failed",
            path = request.requestURI,
            errors = fieldErrors
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message ?: "Bad Request",
            path = request.requestURI
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(
        MethodArgumentTypeMismatchException::class,
        HttpMessageNotReadableException::class,
        ConstraintViolationException::class,
        MissingServletRequestParameterException::class,
        HttpMediaTypeNotSupportedException::class,
        ImportException::class
    )
    fun handleBadRequestExceptions(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val fieldErrors =
            if (e is ConstraintViolationException) {
                e.constraintViolations.map { violation ->
                    FieldError(
                        field = violation.propertyPath.toString().substringAfterLast('.'),
                        message = violation.message
                    )
                }
            } else {
                null
            }
        val response = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            message = e.message ?: "Bad Request",
            path = request.requestURI,
            errors = fieldErrors
        )
        return ResponseEntity.badRequest().body(response)
    }

    @ExceptionHandler(NoHandlerFoundException::class, NoResourceFoundException::class)
    fun handleNotFoundExceptions(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val response = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            message = e.message ?: "Not Found",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response)
    }

    @ExceptionHandler(UndeclaredThrowableException::class)
    fun handleUndeclaredThrowableException(e: UndeclaredThrowableException, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        val cause = e.undeclaredThrowable ?: e.cause ?: e
        return when (cause) {
            is ValidationException, is NotFoundException, is ConflictException, is BadRequestException, is ServiceUnavailableException, is NotImplementedException ->
                handleRestException(cause as Exception, request)
            is EntityNotFoundException -> handleEntityNotFoundException(cause, request)
            is IllegalArgumentException -> handleIllegalArgumentException(cause, request)
            is MethodArgumentNotValidException -> handleValidationExceptions(cause, request)
            is MethodArgumentTypeMismatchException,
            is HttpMessageNotReadableException,
            is ConstraintViolationException,
            is MissingServletRequestParameterException,
            is HttpMediaTypeNotSupportedException,
            is ImportException -> handleBadRequestExceptions(cause as Exception, request)
            else -> handleGenericException(cause as Exception, request)
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(e: Exception, request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        log.error("Unhandled exception: ${e.message}", e)
        val response = ErrorResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            message = "Internal Server Error",
            path = request.requestURI
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response)
    }
}
