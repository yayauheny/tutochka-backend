package yayauheny.by.common.query

import org.jooq.Field

data class FieldMeta<T>(
    val field: Field<T>,
    val parser: (String) -> T?,
    val sortable: Boolean = false,
    val allowedOps: Set<FilterOperator> = FilterOperator.ALLOWED_OPS
)
