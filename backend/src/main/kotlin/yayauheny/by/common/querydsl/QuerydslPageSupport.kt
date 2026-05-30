package yayauheny.by.common.querydsl

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection

fun PaginationRequest.toPageRequest(
    defaultSort: String,
    sortMapper: QuerydslSortMapper<*>
): PageRequest {
    val sortField = sortMapper.resolveSortField(sort, defaultSort)
    val springDirection = if (direction == SortDirection.DESC) Sort.Direction.DESC else Sort.Direction.ASC
    return PageRequest.of(page.coerceAtLeast(0), size.coerceAtLeast(1), Sort.by(springDirection, sortField))
}

fun <T> buildPage(content: List<T>, pageable: Pageable, totalElements: Long): Page<T> =
    PageImpl(content, pageable, totalElements)
