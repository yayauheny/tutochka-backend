package yayauheny.by.common.querydsl

import com.querydsl.core.types.dsl.BooleanExpression

class QuerydslPredicates private constructor() {
    private val predicates = mutableListOf<BooleanExpression>()

    fun <T> add(value: T?, predicateFactory: (T) -> BooleanExpression): QuerydslPredicates {
        if (value != null) {
            predicates.add(predicateFactory(value))
        }
        return this
    }

    fun addIfNotNull(predicate: BooleanExpression?): QuerydslPredicates {
        if (predicate != null) {
            predicates.add(predicate)
        }
        return this
    }

    fun buildAnd(): BooleanExpression? =
        predicates.reduceOrNull { left, right -> left.and(right) }

    fun buildOr(): BooleanExpression? =
        predicates.reduceOrNull { left, right -> left.or(right) }

    companion object {
        fun builder(): QuerydslPredicates = QuerydslPredicates()
    }
}
