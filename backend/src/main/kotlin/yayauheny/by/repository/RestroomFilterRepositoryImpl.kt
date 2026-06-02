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
import yayauheny.by.entity.QRestroomEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.restroom.RestroomFilter

@Repository
class RestroomFilterRepositoryImpl(
    private val entityManager: EntityManager
) : RestroomFilterRepository {
    private val queryFactory = JPAQueryFactory(entityManager)

    private val root = QRestroomEntity.restroomEntity
    private val sortMapper = QuerydslSortMapper<QRestroomEntity>(
        defaultSort = DEFAULT_SORT,
        sortDefinitions = mapOf(
            "id" to { direction -> if (direction == SortDirection.DESC) id.desc() else id.asc() },
            "cityId" to { direction -> if (direction == SortDirection.DESC) city.id.desc() else city.id.asc() },
            "buildingId" to { direction -> if (direction == SortDirection.DESC) building.id.desc() else building.id.asc() },
            "subwayStationId" to { direction -> if (direction == SortDirection.DESC) subwayStation.id.desc() else subwayStation.id.asc() },
            "status" to { direction -> if (direction == SortDirection.DESC) status.desc() else status.asc() },
            "feeType" to { direction -> if (direction == SortDirection.DESC) feeType.desc() else feeType.asc() },
            "genderType" to { direction -> if (direction == SortDirection.DESC) genderType.desc() else genderType.asc() },
            "accessibilityType" to { direction -> if (direction == SortDirection.DESC) accessibilityType.desc() else accessibilityType.asc() },
            "placeType" to { direction -> if (direction == SortDirection.DESC) placeType.desc() else placeType.asc() },
            "locationType" to { direction -> if (direction == SortDirection.DESC) locationType.desc() else locationType.asc() },
            "originProvider" to { direction -> if (direction == SortDirection.DESC) originProvider.desc() else originProvider.asc() },
            "originId" to { direction -> if (direction == SortDirection.DESC) originId.desc() else originId.asc() },
            "isHidden" to { direction -> if (direction == SortDirection.DESC) isHidden.desc() else isHidden.asc() },
            "name" to { direction -> if (direction == SortDirection.DESC) name.desc() else name.asc() },
            "address" to { direction -> if (direction == SortDirection.DESC) address.desc() else address.asc() }
        )
    )

    override fun findAllByFilter(filter: RestroomFilter, pagination: PaginationRequest): Page<RestroomEntity> {
        val pageable = pagination.toPageRequest(DEFAULT_SORT, sortMapper)
        val predicate = QuerydslPredicates.builder()
            .add(filter.cityId) { root.city.id.eq(it) }
            .add(filter.buildingId) { root.building.id.eq(it) }
            .add(filter.subwayStationId) { root.subwayStation.id.eq(it) }
            .add(filter.status) { root.status.eq(it) }
            .add(filter.feeType) { root.feeType.eq(it) }
            .add(filter.genderType) { root.genderType.eq(it) }
            .add(filter.accessibilityType) { root.accessibilityType.eq(it) }
            .add(filter.placeType) { root.placeType.eq(it) }
            .add(filter.locationType) { root.locationType.eq(it) }
            .add(filter.originProvider) { root.originProvider.eq(it) }
            .add(filter.originId?.trim()?.takeIf { it.isNotEmpty() }) { root.originId.eq(it) }
            .add(filter.isHidden) { root.isHidden.eq(it) }
            .add(filter.name?.trim()?.takeIf { it.isNotEmpty() }) { root.name.containsIgnoreCase(it) }
            .add(filter.address?.trim()?.takeIf { it.isNotEmpty() }) { root.address.containsIgnoreCase(it) }
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
        const val DEFAULT_SORT = "id"
    }
}
