package yayauheny.by.util

import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.Page
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest

fun <T> Page<T>.toPageResponse() = PageResponse(
    content = this.content,
    totalElements = this.totalElements.toInt(),
    totalPages = this.totalPages,
    first = this.isFirst,
    last = this.isLast,
    page = this.number,
    size = this.size
)

fun HttpServletRequest.toPaginationRequest(
    defaultPage: Int = 0,
    defaultSize: Int = 20,
    maxSize: Int = 100,
    defaultSort: String? = null
): PaginationRequest {
    val resolvedPage = getParameter("page").toPage(defaultPage)
    val resolvedSize = getParameter("size").toSize(defaultSize, maxSize)
    val resolvedSort = getParameter("sort")?.trim()?.takeIf { it.isNotEmpty() } ?: defaultSort
    val resolvedDirection = getParameter("direction").toSortDirection()

    return PaginationRequest(
        sort = resolvedSort,
        direction = resolvedDirection,
        page = resolvedPage,
        size = resolvedSize
    )
}

private fun String?.toPage(defaultPage: Int): Int =
    this?.toIntOrNull()?.coerceAtLeast(0) ?: defaultPage.coerceAtLeast(0)

private fun String?.toSize(defaultSize: Int, maxSize: Int): Int {
    val parsed = this?.toIntOrNull() ?: defaultSize
    val normalized = if (parsed < 1) 1 else parsed
    return normalized.coerceAtMost(maxSize.coerceAtLeast(1))
}

private fun String?.toSortDirection(): yayauheny.by.common.query.SortDirection =
    when (this?.trim()?.uppercase()) {
        yayauheny.by.common.query.SortDirection.DESC.name -> yayauheny.by.common.query.SortDirection.DESC
        else -> yayauheny.by.common.query.SortDirection.ASC
    }
