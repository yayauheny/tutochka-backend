package yayauheny.by.unit.repository

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.common.errors.BadRequestException
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.model.country.CountryFilter
import yayauheny.by.repository.CountryJpaRepository

class CountryFilterRepositoryTest : RepositorySliceTestBase() {
    @Autowired
    private lateinit var repository: CountryJpaRepository

    @Test
    fun given_trimmed_code_filter_when_find_all_by_filter_then_return_matching_country() {
        DatabaseTestHelper.insertTestCountry(
            DatabaseTestHelper.createTestCountryData(code = "B1", nameRu = "Belarus", nameEn = "Belarus")
        )
        DatabaseTestHelper.insertTestCountry(
            DatabaseTestHelper.createTestCountryData(code = "P1", nameRu = "Poland", nameEn = "Poland")
        )

        val page = repository.findAllByFilter(
            CountryFilter(code = "  b1 "),
            PaginationRequest(sort = "code", direction = SortDirection.ASC, page = 0, size = 10)
        )

        assertThat(page.content.map { it.code }).containsExactly("B1")
    }

    @Test
    fun given_unsupported_sort_when_find_all_by_filter_then_throw_bad_request() {
        DatabaseTestHelper.insertTestCountry()

        assertThatThrownBy {
            repository.findAllByFilter(
                CountryFilter(),
                PaginationRequest(sort = "unsupported", direction = SortDirection.ASC, page = 0, size = 10)
            )
        }.hasCauseInstanceOf(BadRequestException::class.java)
    }
}
