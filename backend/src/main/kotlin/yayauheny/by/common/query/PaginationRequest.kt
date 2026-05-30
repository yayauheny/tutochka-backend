package yayauheny.by.common.query

data class PaginationRequest(
    val filters: List<FilterCriteria> = emptyList(),
    val sort: String? = null,
    val direction: SortDirection = SortDirection.ASC,
    val page: Int = 0,
    val size: Int = 20
)
