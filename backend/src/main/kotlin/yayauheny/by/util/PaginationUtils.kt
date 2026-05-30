package yayauheny.by.util

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
