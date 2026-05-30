package yayauheny.by.util

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import org.springframework.data.domain.Page
import yayauheny.by.common.query.PageResponse

fun <T> Page<T>.toPageResponse() = PageResponse(
    content = this.content,
    totalElements = this.totalElements.toInt(),
    totalPages = this.totalPages,
    first = this.isFirst,
    last = this.isLast,
    page = this.number,
    size = this.size
)

fun PaginationRequest.toPageRequest(defaultSort: String): PageRequest {
    val sortField = sort?.takeIf { it.isNotBlank() } ?: defaultSort
    val directionValue = if (direction == SortDirection.DESC) Sort.Direction.DESC else Sort.Direction.ASC
    return PageRequest.of(page, size, Sort.by(directionValue, sortField))
}
