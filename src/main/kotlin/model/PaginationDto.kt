package yayauheny.by.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Pagination parameters")
data class PaginationDto(
    @Schema(description = "Page number (0-based)", example = "0")
    val page: Int = 0,
    @Schema(description = "Page size", example = "20")
    val size: Int = 20,
    @Schema(description = "Sort field", example = "name")
    val sort: String? = null,
    @Schema(description = "Sort direction", example = "ASC", allowableValues = ["ASC", "DESC"])
    val direction: SortDirection = SortDirection.ASC,
    @Schema(description = "Filter parameters as key-value pairs")
    val filters: Map<String, Any> = emptyMap()
)

@Schema(description = "Sort direction")
enum class SortDirection {
    ASC,
    DESC
}

@Schema(description = "Paginated response")
data class PageResponseDto<T>(
    @Schema(description = "Page content")
    val content: List<T>,
    @Schema(description = "Current page number")
    val page: Int,
    @Schema(description = "Page size")
    val size: Int,
    @Schema(description = "Total number of elements")
    val totalElements: Long,
    @Schema(description = "Total number of pages")
    val totalPages: Int,
    @Schema(description = "Is first page")
    val first: Boolean,
    @Schema(description = "Is last page")
    val last: Boolean
)
