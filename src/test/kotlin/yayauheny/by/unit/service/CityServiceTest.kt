package yayauheny.by.unit.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.service.CityService
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.common.query.PaginationRequest

@DisplayName("CityService Tests")
class CityServiceTest {
    private val cityRepository = mockk<CityRepository>()
    private val countryRepository = mockk<CountryRepository>()
    private val cityService = CityService(cityRepository, countryRepository)

    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        @Test
        @DisplayName("should_retrieve_all_cities")
        fun should_retrieve_all_cities() =
            runTest {
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = TestDataHelpers.createCityList(3),
                        page = 0,
                        size = 10,
                        totalElements = 3,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { cityRepository.findAll(pagination) } returns expectedPage

                val result = cityService.getAllCities(pagination)

                assertEquals(expectedPage, result)
                coVerify { cityRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("should_return_city_when_found_by_id")
        fun should_return_city_when_found_by_id() =
            runTest {
                val city = TestDataHelpers.createCityResponseDto()
                coEvery { cityRepository.findById(city.id) } returns city

                val result = cityService.getCityById(city.id)

                assertEquals(city, result)
                coVerify { cityRepository.findById(city.id) }
            }

        @Test
        @DisplayName("should_return_null_when_city_not_found_by_id")
        fun should_return_null_when_city_not_found_by_id() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityRepository.findById(id) } returns null

                val result = cityService.getCityById(id)

                assertNull(result)
                coVerify { cityRepository.findById(id) }
            }

        @Test
        @DisplayName("should_retrieve_cities_by_country_id")
        fun should_retrieve_cities_by_country_id() =
            runTest {
                val countryId = UUID.randomUUID()
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = TestDataHelpers.createCityList(2),
                        page = 0,
                        size = 10,
                        totalElements = 2,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { cityRepository.findByCountryId(countryId, pagination) } returns expectedPage

                val result = cityService.getCitiesByCountry(countryId, pagination)

                assertEquals(expectedPage, result)
                coVerify { cityRepository.findByCountryId(countryId, pagination) }
            }

        @Test
        @DisplayName("should_search_cities_by_name")
        fun should_search_cities_by_name() =
            runTest {
                val searchName = "Minsk"
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = TestDataHelpers.createCityList(2),
                        page = 0,
                        size = 10,
                        totalElements = 2,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { cityRepository.findByName(searchName, pagination) } returns expectedPage

                val result = cityService.searchCitiesByName(searchName, pagination)

                assertEquals(expectedPage, result)
                coVerify { cityRepository.findByName(searchName, pagination) }
            }
    }

    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        @Test
        @DisplayName("should_create_new_city_with_valid_country")
        fun should_create_new_city_with_valid_country() =
            runTest {
                val country = TestDataHelpers.createCountryResponseDto()
                val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id)
                val expectedResponse = TestDataHelpers.createCityResponseDto(countryId = country.id)
                coEvery { countryRepository.findById(country.id) } returns country
                coEvery { cityRepository.findSingle(any()) } returns null
                coEvery { cityRepository.save(any()) } returns expectedResponse

                val result = cityService.createCity(createDto)

                assertEquals(expectedResponse, result)
                coVerify { countryRepository.findById(country.id) }
                coVerify(exactly = 2) { cityRepository.findSingle(any()) }
                coVerify { cityRepository.save(any()) }
            }

        @Test
        @DisplayName("should_throw_exception_when_country_not_found")
        fun should_throw_exception_when_country_not_found() =
            runTest {
                val countryId = UUID.randomUUID()
                val createDto = TestDataHelpers.createCityCreateDto(countryId = countryId)
                coEvery { countryRepository.findById(countryId) } returns null

                assertThrows<IllegalArgumentException> {
                    cityService.createCity(createDto)
                }

                coVerify { countryRepository.findById(countryId) }
                coVerify(exactly = 0) { cityRepository.save(any()) }
            }

        @Test
        @DisplayName("should_throw_exception_when_city_name_already_exists_in_country")
        fun should_throw_exception_when_city_name_already_exists_in_country() =
            runTest {
                val country = TestDataHelpers.createCountryResponseDto()
                val createDto = TestDataHelpers.createCityCreateDto(countryId = country.id)
                coEvery { countryRepository.findById(country.id) } returns country
                coEvery { cityRepository.findSingle(any()) } returns TestDataHelpers.createCityResponseDto()

                assertThrows<IllegalArgumentException> {
                    cityService.createCity(createDto)
                }

                coVerify { countryRepository.findById(country.id) }
                coVerify { cityRepository.findSingle(any()) }
                coVerify(exactly = 0) { cityRepository.save(any()) }
            }

        @ParameterizedTest
        @ValueSource(doubles = [40.7128, 51.5074, 48.8566, 35.6762, 55.7558])
        @DisplayName("should_handle_different_latitude_values")
        fun should_handle_different_latitude_values(lat: Double) =
            runTest {
                val country = TestDataHelpers.createCountryResponseDto()
                val createDto =
                    TestDataHelpers.createCityCreateDto(
                        countryId = country.id,
                        lat = lat
                    )
                val expectedResponse =
                    TestDataHelpers.createCityResponseDto(
                        countryId = country.id,
                        lat = lat
                    )
                coEvery { countryRepository.findById(country.id) } returns country
                coEvery { cityRepository.findSingle(any()) } returns null
                coEvery { cityRepository.save(any()) } returns expectedResponse

                val result = cityService.createCity(createDto)

                assertEquals(expectedResponse, result)
                coVerify { cityRepository.save(any()) }
            }
    }

    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        @Test
        @DisplayName("should_update_existing_city")
        fun should_update_existing_city() =
            runTest {
                val existingCity = TestDataHelpers.createCityResponseDto()
                val updateDto = TestDataHelpers.createCityUpdateDto()
                val updatedCity =
                    existingCity.copy(
                        nameRu = updateDto.nameRu,
                        nameEn = updateDto.nameEn
                    )
                coEvery { cityRepository.update(any(), any()) } returns updatedCity

                val result = cityService.updateCity(existingCity.id, updateDto)

                assertEquals(updatedCity, result)
                coVerify { cityRepository.update(existingCity.id, updateDto) }
            }

        @Test
        @DisplayName("should_throw_exception_when_updating_non_existent_city")
        fun should_throw_exception_when_updating_non_existent_city() =
            runTest {
                val id = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCityUpdateDto()
                coEvery { cityRepository.update(any(), any()) } throws IllegalStateException("Failed to update city $id")

                assertThrows<IllegalStateException> {
                    cityService.updateCity(id, updateDto)
                }

                coVerify { cityRepository.update(id, updateDto) }
            }
    }

    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        @Test
        @DisplayName("should_return_true_when_city_exists_and_is_deleted")
        fun should_return_true_when_city_exists_and_is_deleted() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityRepository.deleteById(id) } returns true

                val result = cityService.deleteCity(id)

                assertTrue(result)
                coVerify { cityRepository.deleteById(id) }
            }

        @Test
        @DisplayName("should_return_false_when_city_does_not_exist")
        fun should_return_false_when_city_does_not_exist() =
            runTest {
                val id = UUID.randomUUID()
                coEvery { cityRepository.deleteById(id) } returns false

                val result = cityService.deleteCity(id)

                assertFalse(result)
                coVerify { cityRepository.deleteById(id) }
            }
    }
}
