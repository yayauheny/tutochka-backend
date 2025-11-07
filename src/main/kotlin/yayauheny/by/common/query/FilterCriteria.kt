package yayauheny.by.common.query

import kotlinx.serialization.Serializable

@Serializable
data class FilterCriteria(
    val field: String,
    val operator: FilterOperator,
    val value: String
)
