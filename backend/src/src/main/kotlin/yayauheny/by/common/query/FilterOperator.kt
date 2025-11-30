package yayauheny.by.common.query

import kotlinx.serialization.Serializable

@Serializable
enum class FilterOperator {
    EQ, // equals
    NE, // not equals
    GT, // greater than
    GE, // greater or equal
    LT, // less than
    LE, // less or equal
    LIKE, // case-sensitive contains
    ILIKE, // case-insensitive contains
    IN, // in list (comma-separated)
    NOT_IN, // not in list
    ;

    companion object {
        val ALLOWED_OPS: Set<FilterOperator> = entries.toSet()
    }
}
