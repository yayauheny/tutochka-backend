package yayauheny.by.common.query


data class FilterCriteria(
    val field: String,
    val operator: FilterOperator,
    val value: String
)
