package yayauheny.by.common.query.builder

import org.jooq.Condition
import org.jooq.Field
import org.jooq.SortField
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.SortDirection

class QueryBuilder(
    private val fields: Map<String, FieldMeta<*>>
) {
    fun buildFilters(filters: List<FilterCriteria>): List<Condition> =
        filters.mapNotNull { criteria ->
            val meta = fields[criteria.field] ?: return@mapNotNull null
            buildCondition(meta, criteria)
        }

    fun buildSort(
        sortField: String?,
        direction: SortDirection
    ): List<SortField<*>> {
        val meta = sortField?.let { fields[it] } ?: return emptyList()
        if (!meta.sortable) return emptyList()

        val field = meta.field
        return listOf(
            when (direction) {
                SortDirection.ASC -> field.asc()
                SortDirection.DESC -> field.desc()
            }
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> buildCondition(
        meta: FieldMeta<T>,
        criteria: FilterCriteria
    ): Condition? {
        val field = meta.field
        val parser = meta.parser
        val op = criteria.operator
        val raw = criteria.value

        if (op !in meta.allowedOps) return null

        // parse once
        when (op) {
            FilterOperator.IN, FilterOperator.NOT_IN -> {
                val values = raw.split(",").mapNotNull { parser(it.trim()) }
                if (values.isEmpty()) return null
                return if (op == FilterOperator.IN) {
                    // field.`in` accepts Collection
                    (field as Field<Any?>).`in`(values)
                } else {
                    (field as Field<Any?>).notIn(values)
                }
            }
            else -> {
                val parsed = parser(raw) ?: return null

                return when (op) {
                    FilterOperator.EQ -> (field as Field<Any?>).eq(parsed)
                    FilterOperator.NE -> (field as Field<Any?>).ne(parsed)
                    FilterOperator.GT -> {
                        if (parsed is Comparable<*>) {
                            val f = field as Field<Comparable<Any?>>
                            f.gt(parsed as Comparable<Any?>)
                        } else {
                            null
                        }
                    }
                    FilterOperator.GE -> {
                        if (parsed is Comparable<*>) {
                            val f = field as Field<Comparable<Any?>>
                            f.ge(parsed as Comparable<Any?>)
                        } else {
                            null
                        }
                    }
                    FilterOperator.LT -> {
                        if (parsed is Comparable<*>) {
                            val f = field as Field<Comparable<Any?>>
                            f.lt(parsed as Comparable<Any?>)
                        } else {
                            null
                        }
                    }
                    FilterOperator.LE -> {
                        if (parsed is Comparable<*>) {
                            val f = field as Field<Comparable<Any?>>
                            f.le(parsed as Comparable<Any?>)
                        } else {
                            null
                        }
                    }
                    FilterOperator.LIKE -> {
                        (parsed as? String)?.let { (field as Field<String?>).like("%$it%") }
                    }
                    FilterOperator.ILIKE -> {
                        (parsed as? String)?.let { (field as Field<String?>).likeIgnoreCase("%$it%") }
                    }
                    else -> null
                }
            }
        }
    }
}
