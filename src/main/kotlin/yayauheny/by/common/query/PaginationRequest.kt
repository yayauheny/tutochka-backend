package yayauheny.by.common.query

import kotlinx.serialization.Serializable
import yayauheny.by.config.ApiConstants

@Serializable
data class PaginationRequest(
    val filters: List<FilterCriteria> = emptyList(),
    val sort: String? = null,
    val direction: SortDirection = SortDirection.ASC,
    val page: Int = ApiConstants.DEFAULT_PAGE,
    val size: Int = ApiConstants.DEFAULT_PAGE_SIZE
)
