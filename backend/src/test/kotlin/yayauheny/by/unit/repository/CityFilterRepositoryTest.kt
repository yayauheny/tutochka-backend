package yayauheny.by.unit.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.entity.CityEntity
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.model.city.CityFilter
import yayauheny.by.repository.CityJpaRepository

class CityFilterRepositoryTest : RepositorySliceTestBase() {
    @Autowired
    private lateinit var repository: CityJpaRepository

    @Test
    fun given_name_and_country_filters_when_find_all_by_filter_then_match_ru_and_en_names() {
        val belarusId = DatabaseTestHelper.insertTestCountry(
            DatabaseTestHelper.createTestCountryData(code = "B1", nameRu = "Belarus", nameEn = "Belarus")
        )
        val polandId = DatabaseTestHelper.insertTestCountry(
            DatabaseTestHelper.createTestCountryData(code = "P1", nameRu = "Poland", nameEn = "Poland")
        )

        insertCity(belarusId, "Минск", "Minsk", "Minsk Region")
        insertCity(belarusId, "Гродно", "Hrodna", "Grodno Region")
        insertCity(polandId, "Минск-Мазовецкий", "Minsk Mazowiecki", "Masovian")

        val page = repository.findAllByFilter(
            CityFilter(countryId = belarusId, name = " minsk "),
            PaginationRequest(sort = "nameEn", direction = SortDirection.ASC, page = 0, size = 10)
        )

        assertThat(page.content.map(CityEntity::nameEn)).containsExactly("Minsk")
    }

    @Test
    fun given_region_sort_and_page_when_find_all_by_filter_then_return_requested_slice() {
        val countryId = DatabaseTestHelper.insertTestCountry()
        insertCity(countryId, "Брест", "Brest", "West")
        insertCity(countryId, "Витебск", "Vitebsk", "North")
        insertCity(countryId, "Гомель", "Gomel", "East")

        val page = repository.findAllByFilter(
            CityFilter(countryId = countryId),
            PaginationRequest(sort = "region", direction = SortDirection.ASC, page = 1, size = 1)
        )

        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.content.map(CityEntity::region)).containsExactly("North")
    }

    private fun insertCity(
        countryId: java.util.UUID,
        nameRu: String,
        nameEn: String,
        region: String
    ) {
        val cityId = DatabaseTestHelper.insertTestCity(
            countryId,
            DatabaseTestHelper.createTestCityData(nameRu = nameRu, nameEn = nameEn)
        )
        val city = cityRepository.findById(cityId).orElseThrow()
        city.region = region
        cityRepository.saveAndFlush(city)
    }
}
