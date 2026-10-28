package yayauheny.by.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Page
import org.springframework.stereotype.Repository
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.common.querydsl.QuerydslPredicates
import yayauheny.by.common.querydsl.QuerydslSortMapper
import yayauheny.by.common.querydsl.buildPage
import yayauheny.by.common.querydsl.toPageRequest
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.QCityEntity
import yayauheny.by.model.city.CityFilter

@Repository
class CityFilterRepositoryImpl(
    private val entityManager: EntityManager
) : CityFilterRepository {
    private val queryFactory = JPAQueryFactory(entityManager)

    private val root = QCityEntity.cityEntity
    private val sortMapper = QuerydslSortMapper<QCityEntity>(
        defaultSort = DEFAULT_SORT,
        sortDefinitions = mapOf(
            "id" to { direction -> if (direction == SortDirection.DESC) id.desc() else id.asc() },
            "countryId" to { direction -> if (direction == SortDirection.DESC) country.id.desc() else country.id.asc() },
            "nameRu" to { direction -> if (direction == SortDirection.DESC) nameRu.desc() else nameRu.asc() },
            "nameEn" to { direction -> if (direction == SortDirection.DESC) nameEn.desc() else nameEn.asc() },
            "region" to { direction -> if (direction == SortDirection.DESC) region.desc() else region.asc() }
        )
    )

    override fun findAllByFilter(filter: CityFilter, pagination: PaginationRequest): Page<CityEntity> {
        val pageable = pagination.toPageRequest(DEFAULT_SORT, sortMapper)
        val predicate = QuerydslPredicates.builder()
            .add(filter.countryId) { root.country.id.eq(it) }
            .add(filter.region?.trim()?.takeIf { it.isNotEmpty() }) { root.region.containsIgnoreCase(it) }
            .addIfNotNull(namePredicate(filter.name))
            .buildAnd()

        val contentQuery = queryFactory.selectFrom(root)
        if (predicate != null) {
            contentQuery.where(predicate)
        }
        contentQuery.orderBy(sortMapper.toOrderSpecifier(root, pagination.sort, pagination.direction))

        val countQuery = queryFactory.select(root.count()).from(root)
        if (predicate != null) {
            countQuery.where(predicate)
        }

        val content = contentQuery
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()
        val total = countQuery.fetchOne() ?: 0L
        return buildPage(content, pageable, total)
    }

    private fun namePredicate(name: String?): BooleanExpression? {
        val value = name?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return QuerydslPredicates.builder()
            .addIfNotNull(root.nameRu.containsIgnoreCase(value))
            .addIfNotNull(root.nameEn.containsIgnoreCase(value))
            .buildOr()
    }

    private companion object {
        const val DEFAULT_SORT = "nameRu"
    }
}
