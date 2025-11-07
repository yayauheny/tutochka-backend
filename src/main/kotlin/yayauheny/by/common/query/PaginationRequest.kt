package yayauheny.by.common.query

import kotlinx.serialization.Serializable

@Serializable
data class PaginationRequest(
    val filters: List<FilterCriteria> = emptyList(),
    val sort: String? = null,
    val direction: SortDirection = SortDirection.ASC,
    val page: Int = 0,
    val size: Int = 10
)
