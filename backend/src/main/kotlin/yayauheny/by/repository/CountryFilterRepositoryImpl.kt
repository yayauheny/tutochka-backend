package yayauheny.by.repository

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
import yayauheny.by.entity.CountryEntity
import yayauheny.by.entity.QCountryEntity
import yayauheny.by.model.country.CountryFilter

@Repository
class CountryFilterRepositoryImpl(
    private val entityManager: EntityManager
) : CountryFilterRepository {
    private val queryFactory = JPAQueryFactory(entityManager)

    private val root = QCountryEntity.countryEntity
    private val sortMapper = QuerydslSortMapper<QCountryEntity>(
        defaultSort = DEFAULT_SORT,
        sortDefinitions = mapOf(
            "id" to { direction -> if (direction == SortDirection.DESC) id.desc() else id.asc() },
            "code" to { direction -> if (direction == SortDirection.DESC) code.desc() else code.asc() },
            "nameRu" to { direction -> if (direction == SortDirection.DESC) nameRu.desc() else nameRu.asc() },
            "nameEn" to { direction -> if (direction == SortDirection.DESC) nameEn.desc() else nameEn.asc() },
            "isDeleted" to { direction -> if (direction == SortDirection.DESC) isDeleted.desc() else isDeleted.asc() }
        )
    )

    override fun findAllByFilter(filter: CountryFilter, pagination: PaginationRequest): Page<CountryEntity> {
        val pageable = pagination.toPageRequest(DEFAULT_SORT, sortMapper)
        val predicate = QuerydslPredicates.builder()
            .add(filter.code?.trim()?.takeIf { it.isNotEmpty() }) { root.code.containsIgnoreCase(it) }
            .add(filter.nameRu?.trim()?.takeIf { it.isNotEmpty() }) { root.nameRu.containsIgnoreCase(it) }
            .add(filter.nameEn?.trim()?.takeIf { it.isNotEmpty() }) { root.nameEn.containsIgnoreCase(it) }
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

    private companion object {
        const val DEFAULT_SORT = "nameRu"
    }
}
