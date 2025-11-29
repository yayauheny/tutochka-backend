package yayauheny.by.common.errors

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema(description = "Field validation error")
data class FieldError(
    @Schema(description = "Field name", example = "nameRu")
    val field: String,
    @Schema(description = "Error message", example = "nameRu must not be blank")
    val message: String
)

@Serializable
@Schema(description = "Error response structure")
data class ErrorResponse(
    @Schema(description = "HTTP status code", example = "400")
    val status: Int,
    @Schema(description = "Detailed error message", example = "Invalid UUID format")
    val message: String?,
    @Schema(description = "Request path where error occurred", example = "/api/v1/countries/123")
    val path: String,
    @Schema(description = "Field validation errors (only present for validation errors)")
    val errors: List<FieldError>? = null
)
