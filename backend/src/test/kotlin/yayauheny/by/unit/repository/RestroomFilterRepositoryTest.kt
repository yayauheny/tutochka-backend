package yayauheny.by.unit.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.repository.RestroomJpaRepository

class RestroomFilterRepositoryTest : RepositorySliceTestBase() {
    @Autowired
    private lateinit var repository: RestroomJpaRepository

    @Test
    fun given_status_origin_and_visibility_filters_when_find_all_by_filter_then_return_matching_restroom() {
        val cityId = DatabaseTestHelper.createTestEnvironment().cityId

        DatabaseTestHelper.insertTestRestroom(
            cityId,
            DatabaseTestHelper.createTestRestroomData(
                name = "Visible restroom",
                address = "Address A",
                status = RestroomStatus.ACTIVE,
                originProvider = ImportProvider.TWO_GIS,
                originId = "alpha"
            )
        )
        DatabaseTestHelper.insertTestRestroom(
            cityId,
            DatabaseTestHelper.createTestRestroomData(
                name = "Hidden restroom",
                address = "Address B",
                status = RestroomStatus.ACTIVE,
                originProvider = ImportProvider.TWO_GIS,
                originId = "beta"
            )
        ).also { restroomId ->
            val restroom = restroomRepository.findById(restroomId).orElseThrow()
            restroom.isHidden = true
            restroomRepository.saveAndFlush(restroom)
        }

        val page = repository.findAllByFilter(
            RestroomFilter(
                cityId = cityId,
                status = RestroomStatus.ACTIVE,
                originProvider = ImportProvider.TWO_GIS,
                originId = "  alpha ",
                isHidden = false
            ),
            PaginationRequest(sort = "address", direction = SortDirection.ASC, page = 0, size = 10)
        )

        assertThat(page.content.map(RestroomEntity::name)).containsExactly("Visible restroom")
    }

    @Test
    fun given_sorted_page_when_find_all_by_filter_then_return_expected_order() {
        val cityId = DatabaseTestHelper.createTestEnvironment().cityId

        DatabaseTestHelper.insertTestRestroom(
            cityId,
            DatabaseTestHelper.createTestRestroomData(name = "First", address = "B Street")
        )
        DatabaseTestHelper.insertTestRestroom(
            cityId,
            DatabaseTestHelper.createTestRestroomData(name = "Second", address = "A Street")
        )
        DatabaseTestHelper.insertTestRestroom(
            cityId,
            DatabaseTestHelper.createTestRestroomData(name = "Third", address = "C Street")
        )

        val page = repository.findAllByFilter(
            RestroomFilter(cityId = cityId),
            PaginationRequest(sort = "address", direction = SortDirection.ASC, page = 0, size = 2)
        )

        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.content.map(RestroomEntity::address)).containsExactly("A Street", "B Street")
    }
}
