package yayauheny.by.common.query

import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectLimitStep
import org.jooq.SelectWhereStep
import org.jooq.impl.DSL
import yayauheny.by.common.query.builder.QueryBuilder

fun <R : Record> SelectWhereStep<R>.applyFilters(
    builder: QueryBuilder,
    filters: List<FilterCriteria>
): SelectConditionStep<R> {
    val conditions = builder.buildFilters(filters)
    return if (conditions.isEmpty()) {
        this.where(DSL.noCondition())
    } else {
        this.where(DSL.and(conditions))
    }
}

fun <R : Record> SelectWhereStep<R>.applySorting(
    builder: QueryBuilder,
    sort: String?,
    direction: SortDirection
): SelectLimitStep<R> {
    val temp = this as SelectConditionStep<R>
    val sortFields = builder.buildSort(sort, direction)
    return if (sortFields.isEmpty()) {
        temp
    } else {
        temp.orderBy(sortFields)
    }
}

fun <R : Record> SelectConditionStep<R>.applyFilters(
    builder: QueryBuilder,
    filters: List<FilterCriteria>
): SelectConditionStep<R> {
    val conditions = builder.buildFilters(filters)
    return if (conditions.isEmpty()) {
        this
    } else {
        this.and(DSL.and(conditions))
    }
}

fun <R : Record> SelectConditionStep<R>.applySorting(
    builder: QueryBuilder,
    sort: String?,
    direction: SortDirection
): SelectLimitStep<R> {
    val sortFields = builder.buildSort(sort, direction)
    return if (sortFields.isEmpty()) {
        this
    } else {
        this.orderBy(sortFields)
    }
}
