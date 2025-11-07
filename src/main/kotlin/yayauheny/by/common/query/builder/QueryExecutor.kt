package yayauheny.by.common.query.builder

import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectConditionStep
import org.jooq.SelectWhereStep
import org.jooq.impl.DSL
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.common.query.applyFilters
import yayauheny.by.common.query.applySorting

class QueryExecutor<T>(
    private val ctx: DSLContext,
    private val mapper: (Record) -> T
) {
    fun executePaginated(
        baseQuery: SelectWhereStep<*>,
        request: PaginationRequest,
        builder: QueryBuilder,
        additionalConditions: List<Condition> = emptyList(),
        fetchCount: Boolean = true
    ): PageResponse<T> {
        val filtered = baseQuery
            .applyFilters(builder, request.filters)
            .applyAdditionalConditions(additionalConditions)
        return executePaginatedInternal(filtered, request, builder, fetchCount)
    }

    fun executePaginated(
        baseQuery: SelectConditionStep<*>,
        request: PaginationRequest,
        builder: QueryBuilder,
        additionalConditions: List<Condition> = emptyList(),
        fetchCount: Boolean = true
    ): PageResponse<T> {
        val filtered = baseQuery
            .applyFilters(builder, request.filters)
            .applyAdditionalConditions(additionalConditions)
        return executePaginatedInternal(filtered, request, builder, fetchCount)
    }

    fun <R : Record> executeSingle(
        baseQuery: SelectWhereStep<R>,
        filters: List<FilterCriteria>,
        sort: String? = null,
        direction: SortDirection = SortDirection.ASC,
        builder: QueryBuilder,
        additionalConditions: List<Condition> = emptyList()
    ): T? {
        val filtered = baseQuery
            .applyFilters(builder, filters)
            .applyAdditionalConditions(additionalConditions)
        val sorted = filtered.applySorting(builder, sort, direction)
        return sorted.limit(1).fetchOne()?.map(mapper)
    }

    fun <R : Record> executeSingle(
        baseQuery: SelectConditionStep<R>,
        filters: List<FilterCriteria>,
        sort: String? = null,
        direction: SortDirection = SortDirection.ASC,
        builder: QueryBuilder,
        additionalConditions: List<Condition> = emptyList()
    ): T? {
        val filtered = baseQuery.applyFilters(builder, filters)
        val withAdditionalConditions = filtered.applyAdditionalConditions(additionalConditions)
        val sorted = withAdditionalConditions.applySorting(builder, sort, direction)
        return sorted.limit(1).fetchOne()?.map(mapper)
    }

    fun <R : Record> executeList(
        baseQuery: SelectWhereStep<R>,
        filters: List<FilterCriteria>,
        sort: String? = null,
        direction: SortDirection,
        builder: QueryBuilder,
        additionalConditions: List<Condition> = emptyList()
    ): List<T> {
        val filtered = baseQuery
            .applyFilters(builder, filters)
            .applyAdditionalConditions(additionalConditions)
        val sorted = filtered.applySorting(builder, sort, direction)
        return sorted.fetch().map(mapper)
    }

    private fun executePaginatedInternal(
        filtered: SelectConditionStep<*>,
        request: PaginationRequest,
        builder: QueryBuilder,
        fetchCount: Boolean
    ): PageResponse<T> {
        val totalElements = if (fetchCount) ctx.fetchCount(filtered) else 0
        val sorted = filtered.applySorting(builder, request.sort, request.direction)
        val offset = ((request.page - 1).coerceAtLeast(0)) * request.size
        val content =
            sorted
                .limit(request.size)
                .offset(offset)
                .fetch()
                .map(mapper)

        val totalPages =
            if (request.size > 0 && fetchCount) {
                ((totalElements + request.size - 1) / request.size)
            } else {
                0
            }

        return PageResponse(
            content = content,
            page = request.page,
            size = request.size,
            totalElements = totalElements,
            totalPages = totalPages,
            first = request.page <= 1,
            last = fetchCount && request.page >= totalPages
        )
    }
}

private fun <R : Record> SelectConditionStep<R>.applyAdditionalConditions(
    extraConditions: List<Condition>
): SelectConditionStep<R> {
    var step = this
    extraConditions.forEach { step = step.and(it) }
    return step
}

private fun <R : Record> SelectWhereStep<R>.applyAdditionalConditions(
    extraConditions: List<Condition>
): SelectConditionStep<R> {
    var step: SelectConditionStep<R> = this.where(DSL.trueCondition())
    extraConditions.forEach { step = step.and(it) }
    return step
}
