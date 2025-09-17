package yayauheny.by.util

import kotlin.math.ceil
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto

fun <T> List<T>.toPageResponse(pagination: PaginationDto): PageResponseDto<T> {
    val startIndex = pagination.page * pagination.size
    val endIndex = minOf(startIndex + pagination.size, size)
    val pageContent = if (startIndex < endIndex) subList(startIndex, endIndex) else emptyList()

    val totalElements = size.toLong()
    val totalPages = if (pagination.size > 0) {
        ceil(totalElements.toDouble() / pagination.size).toInt()
    } else 0

    return PageResponseDto(
        content = pageContent,
        page = pagination.page,
        size = pagination.size,
        totalElements = totalElements,
        totalPages = totalPages,
        first = pagination.page == 0,
        last = pagination.page >= totalPages - 1 || totalPages == 0
    )
}

fun createPaginationFromParams(
    page: Int?,
    size: Int?,
    sort: String?
): PaginationDto = PaginationDto(
    page = page ?: 0,
    size = size ?: 20,
    sort = sort
)
