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
import yayauheny.by.common.errors.ConflictException
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.repository.CountryRepository
import yayauheny.by.service.CountryService
import yayauheny.by.helpers.TestDataHelpers

@DisplayName("CountryService Tests")
class CountryServiceTest {
    private val countryRepository = mockk<CountryRepository>()
    private val countryService = CountryService(countryRepository)

    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        @Test
        @DisplayName("should_retrieve_all_countries")
        fun should_retrieve_all_countries() =
            runTest {
                // Given
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = TestDataHelpers.createCountryList(3),
                        page = 0,
                        size = 10,
                        totalElements = 3,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { countryRepository.findAll(pagination) } returns expectedPage

                // When
                val result = countryService.getAllCountries(pagination)

                // Then
                assertEquals(expectedPage, result)
                coVerify { countryRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("should_retrieve_paginated_countries")
        fun should_retrieve_paginated_countries() =
            runTest {
                // Given
                val pagination = PaginationRequest(page = 0, size = 3)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = TestDataHelpers.createCountryList(3),
                        page = 0,
                        size = 3,
                        totalElements = 5,
                        totalPages = 2,
                        first = true,
                        last = false
                    )
                coEvery { countryRepository.findAll(pagination) } returns expectedPage

                // When
                val result = countryService.getAllCountries(pagination)

                // Then
                assertEquals(expectedPage, result)
                coVerify { countryRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("should_handle_empty_pagination")
        fun should_handle_empty_pagination() =
            runTest {
                // Given
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    yayauheny.by.common.query.PageResponse(
                        content = emptyList<CountryResponseDto>(),
                        page = 0,
                        size = 10,
                        totalElements = 0,
                        totalPages = 0,
                        first = true,
                        last = true
                    )
                coEvery { countryRepository.findAll(pagination) } returns expectedPage

                // When
                val result = countryService.getAllCountries(pagination)

                // Then
                assertEquals(expectedPage, result)
                coVerify { countryRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("should_return_country_when_found_by_id")
        fun should_return_country_when_found_by_id() =
            runTest {
                // Given
                val country = TestDataHelpers.createCountryResponseDto()
                coEvery { countryRepository.findById(country.id) } returns country

                // When
                val result = countryService.getCountryById(country.id)

                // Then
                assertEquals(country, result)
                coVerify { countryRepository.findById(country.id) }
            }

        @Test
        @DisplayName("should_return_null_when_country_not_found_by_id")
        fun should_return_null_when_country_not_found_by_id() =
            runTest {
                // Given
                val id = UUID.randomUUID()
                coEvery { countryRepository.findById(id) } returns null

                // When
                val result = countryService.getCountryById(id)

                // Then
                assertNull(result)
                coVerify { countryRepository.findById(id) }
            }

        @Test
        @DisplayName("should_return_country_when_found_by_code")
        fun should_return_country_when_found_by_code() =
            runTest {
                // Given
                val country = TestDataHelpers.createCountryResponseDto()
                coEvery { countryRepository.findSingle(any()) } returns country

                // When
                val result = countryService.getCountryByCode(country.code)

                // Then
                assertEquals(country, result)
                coVerify { countryRepository.findSingle(any()) }
            }

        @Test
        @DisplayName("should_return_null_when_country_not_found_by_code")
        fun should_return_null_when_country_not_found_by_code() =
            runTest {
                // Given
                val code = "XX"
                coEvery { countryRepository.findSingle(any()) } returns null

                // When
                val result = countryService.getCountryByCode(code)

                // Then
                assertNull(result)
                coVerify { countryRepository.findSingle(any()) }
            }

        @Test
        @DisplayName("should_return_true_when_country_exists_by_code")
        fun should_return_true_when_country_exists_by_code() =
            runTest {
                // Given
                val code = "US"
                coEvery { countryRepository.findSingle(any()) } returns TestDataHelpers.createCountryResponseDto()

                // When
                val result = countryService.countryExists(code)

                // Then
                assertTrue(result)
                coVerify { countryRepository.findSingle(any()) }
            }

        @Test
        @DisplayName("should_return_false_when_country_does_not_exist_by_code")
        fun should_return_false_when_country_does_not_exist_by_code() =
            runTest {
                // Given
                val code = "XX"
                coEvery { countryRepository.findSingle(any()) } returns null

                // When
                val result = countryService.countryExists(code)

                // Then
                assertFalse(result)
                coVerify { countryRepository.findSingle(any()) }
            }
    }

    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        @Test
        @DisplayName("should_create_new_country_with_unique_code")
        fun should_create_new_country_with_unique_code() =
            runTest {
                // Given
                val createDto = TestDataHelpers.createCountryCreateDto()
                val expectedResponse = TestDataHelpers.createCountryResponseDto()
                coEvery { countryRepository.findSingle(any()) } returns null
                coEvery { countryRepository.save(any()) } returns expectedResponse

                // When
                val result = countryService.createCountry(createDto)

                // Then
                assertEquals(expectedResponse, result)
                coVerify { countryRepository.findSingle(any()) }
                coVerify { countryRepository.save(any()) }
            }

        @Test
        @DisplayName("should_throw_exception_when_creating_country_with_existing_code")
        fun should_throw_exception_when_creating_country_with_existing_code() =
            runTest {
                // Given
                val createDto = TestDataHelpers.createCountryCreateDto()
                coEvery { countryRepository.findSingle(any()) } returns TestDataHelpers.createCountryResponseDto()

                // When & Then
                assertThrows<ConflictException> {
                    countryService.createCountry(createDto)
                }

                coVerify { countryRepository.findSingle(any()) }
                coVerify(exactly = 0) { countryRepository.save(any()) }
            }

        @ParameterizedTest
        @ValueSource(strings = ["US", "CA", "GB", "DE", "FR"])
        @DisplayName("should_handle_different_country_codes")
        fun should_handle_different_country_codes(code: String) =
            runTest {
                // Given
                val createDto = TestDataHelpers.createCountryCreateDto(code = code)
                val expectedResponse = TestDataHelpers.createCountryResponseDto(code = code)
                coEvery { countryRepository.findSingle(any()) } returns null
                coEvery { countryRepository.save(any()) } returns expectedResponse

                // When
                val result = countryService.createCountry(createDto)

                // Then
                assertEquals(expectedResponse, result)
                coVerify { countryRepository.findSingle(any()) }
                coVerify { countryRepository.save(any()) }
            }
    }

    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        @Test
        @DisplayName("should_update_existing_country_with_same_code")
        fun should_update_existing_country_with_same_code() =
            runTest {
                // Given
                val existingCountry = TestDataHelpers.createCountryResponseDto()
                val updateDto = TestDataHelpers.createCountryUpdateDto()
                val updatedCountry =
                    existingCountry.copy(
                        nameRu = updateDto.nameRu,
                        nameEn = updateDto.nameEn
                    )
                coEvery { countryRepository.update(any(), any()) } returns updatedCountry

                // When
                val result = countryService.updateCountry(existingCountry.id, updateDto)

                // Then
                assertEquals(updatedCountry, result)
                coVerify { countryRepository.update(any(), any()) }
            }

        @Test
        @DisplayName("should_update_existing_country_with_new_unique_code")
        fun should_update_existing_country_with_new_unique_code() =
            runTest {
                // Given
                val existingCountry = TestDataHelpers.createCountryResponseDto()
                val updateDto = TestDataHelpers.createCountryUpdateDto()
                val updatedCountry =
                    existingCountry.copy(
                        nameRu = updateDto.nameRu,
                        nameEn = updateDto.nameEn
                    )
                coEvery { countryRepository.findById(existingCountry.id) } returns existingCountry
                coEvery { countryRepository.update(any(), any()) } returns updatedCountry

                // When
                val result = countryService.updateCountry(existingCountry.id, updateDto)

                // Then
                assertEquals(updatedCountry, result)
                coVerify { countryRepository.update(any(), any()) }
            }

        @Test
        @DisplayName("should_throw_exception_when_updating_with_existing_code")
        fun should_throw_exception_when_updating_with_existing_code() =
            runTest {
                // Given
                val existingCountry = TestDataHelpers.createCountryResponseDto()
                val updateDto = TestDataHelpers.createCountryUpdateDto()
                coEvery { countryRepository.findById(existingCountry.id) } returns existingCountry
                coEvery { countryRepository.update(any(), any()) } throws ConflictException("Страна с кодом 'EXISTING' уже существует")

                // When & Then
                assertThrows<ConflictException> {
                    countryService.updateCountry(existingCountry.id, updateDto)
                }

                coVerify { countryRepository.update(any(), any()) }
            }

        @Test
        @DisplayName("should_return_null_when_updating_non_existent_country")
        fun should_return_null_when_updating_non_existent_country() =
            runTest {
                // Given
                val id = UUID.randomUUID()
                val updateDto = TestDataHelpers.createCountryUpdateDto()
                coEvery { countryRepository.update(any(), any()) } throws
                    yayauheny.by.common.errors
                        .NotFoundException("Country not found")

                // When & Then
                assertThrows<yayauheny.by.common.errors.NotFoundException> {
                    countryService.updateCountry(id, updateDto)
                }

                coVerify { countryRepository.update(any(), any()) }
            }
    }

    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        @Test
        @DisplayName("should_return_true_when_country_exists_and_is_deleted")
        fun should_return_true_when_country_exists_and_is_deleted() =
            runTest {
                // Given
                val id = UUID.randomUUID()
                coEvery { countryRepository.deleteById(id) } returns true

                // When
                val result = countryService.deleteCountry(id)

                // Then
                assertTrue(result)
                coVerify { countryRepository.deleteById(id) }
            }

        @Test
        @DisplayName("should_return_false_when_country_does_not_exist")
        fun should_return_false_when_country_does_not_exist() =
            runTest {
                // Given
                val id = UUID.randomUUID()
                coEvery { countryRepository.deleteById(id) } returns false

                // When
                val result = countryService.deleteCountry(id)

                // Then
                assertFalse(result)
                coVerify { countryRepository.deleteById(id) }
            }
    }
}
