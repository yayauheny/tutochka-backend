package yayauheny.by.common.errors

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import jakarta.servlet.http.HttpServletRequest

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(
        ValidationException::class, 
        NotFoundException::class, 
        ConflictException::class, 
        BadRequestException::class, 
        ServiceUnavailableException::class
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
        return ResponseEntity.status(status).body(response)
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
