package yayauheny.by.common.querydsl

import com.querydsl.core.types.OrderSpecifier
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.common.query.SortDirection

class QuerydslSortMapper<T>(
    private val defaultSort: String,
    private val sortDefinitions: Map<String, T.(SortDirection) -> OrderSpecifier<*>>
) {
    fun resolveSortField(requestedSort: String?, fallbackSort: String = defaultSort): String {
        val resolved = requestedSort?.trim()?.takeIf { it.isNotEmpty() } ?: fallbackSort
        if (!sortDefinitions.containsKey(resolved)) {
            throw BadRequestException("Unsupported sort field '$resolved'")
        }
        return resolved
    }

    fun toOrderSpecifier(root: T, requestedSort: String?, direction: SortDirection): OrderSpecifier<*> {
        val sortField = resolveSortField(requestedSort)
        return sortDefinitions.getValue(sortField).invoke(root, direction)
    }
}
