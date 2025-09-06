package yayauheny.by.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.repository.CountryRepository
import yayauheny.by.testdata.CountryTestData
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("CountryService Tests")
class CountryServiceTest {
    
    private val countryRepository = mockk<CountryRepository>()
    private val countryService = CountryService(countryRepository)
    
    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        
        @Test
        @DisplayName("should_retrieve_all_countries")
        fun should_retrieve_all_countries() = runTest {
            val countries = CountryTestData.createCountryList(3)
            coEvery { countryRepository.findAll() } returns countries
            
            val result = countryService.getAllCountries()
            
            assertEquals(countries, result)
            coVerify { countryRepository.findAll() }
        }
        
        @Test
        @DisplayName("should_return_country_when_found_by_id")
        fun should_return_country_when_found_by_id() = runTest {
            val country = CountryTestData.createCountryResponseDto()
            coEvery { countryRepository.findById(country.id) } returns country
            
            val result = countryService.getCountryById(country.id)
            
            assertEquals(country, result)
            coVerify { countryRepository.findById(country.id) }
        }
        
        @Test
        @DisplayName("should_return_null_when_country_not_found_by_id")
        fun should_return_null_when_country_not_found_by_id() = runTest {
            val id = UUID.randomUUID()
            coEvery { countryRepository.findById(id) } returns null
            
            val result = countryService.getCountryById(id)
            
            assertNull(result)
            coVerify { countryRepository.findById(id) }
        }
        
        @Test
        @DisplayName("should_return_country_when_found_by_code")
        fun should_return_country_when_found_by_code() = runTest {
            val country = CountryTestData.createCountryResponseDto()
            coEvery { countryRepository.findByCode(country.code) } returns country
            
            val result = countryService.getCountryByCode(country.code)
            
            assertEquals(country, result)
            coVerify { countryRepository.findByCode(country.code) }
        }
        
        @Test
        @DisplayName("should_return_null_when_country_not_found_by_code")
        fun should_return_null_when_country_not_found_by_code() = runTest {
            val code = "XX"
            coEvery { countryRepository.findByCode(code) } returns null
            
            val result = countryService.getCountryByCode(code)
            
            assertNull(result)
            coVerify { countryRepository.findByCode(code) }
        }
        
        @Test
        @DisplayName("should_return_true_when_country_exists_by_code")
        fun should_return_true_when_country_exists_by_code() = runTest {
            val code = "US"
            coEvery { countryRepository.existsByCode(code) } returns true
            
            val result = countryService.countryExists(code)
            
            assertTrue(result)
            coVerify { countryRepository.existsByCode(code) }
        }
        
        @Test
        @DisplayName("should_return_false_when_country_does_not_exist_by_code")
        fun should_return_false_when_country_does_not_exist_by_code() = runTest {
            val code = "XX"
            coEvery { countryRepository.existsByCode(code) } returns false
            
            val result = countryService.countryExists(code)
            
            assertFalse(result)
            coVerify { countryRepository.existsByCode(code) }
        }
    }
    
    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        
        @Test
        @DisplayName("should_create_new_country_with_unique_code")
        fun should_create_new_country_with_unique_code() = runTest {
            val createDto = CountryTestData.createCountryCreateDto()
            val expectedResponse = CountryTestData.createCountryResponseDto()
            coEvery { countryRepository.existsByCode(createDto.code) } returns false
            coEvery { countryRepository.save(any()) } returns expectedResponse
            
            val result = countryService.createCountry(createDto)
            
            assertEquals(expectedResponse, result)
            coVerify { countryRepository.existsByCode(createDto.code) }
            coVerify { countryRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_throw_exception_when_creating_country_with_existing_code")
        fun should_throw_exception_when_creating_country_with_existing_code() = runTest {
            val createDto = CountryTestData.createCountryCreateDto()
            coEvery { countryRepository.existsByCode(createDto.code) } returns true
            
            assertThrows<IllegalArgumentException> {
                countryService.createCountry(createDto)
            }
            
            coVerify { countryRepository.existsByCode(createDto.code) }
            coVerify(exactly = 0) { countryRepository.save(any()) }
        }
        
        @ParameterizedTest
        @ValueSource(strings = ["US", "CA", "GB", "DE", "FR"])
        @DisplayName("should_handle_different_country_codes")
        fun should_handle_different_country_codes(code: String) = runTest {
            val createDto = CountryTestData.createCountryCreateDto(code = code)
            val expectedResponse = CountryTestData.createCountryResponseDto(code = code)
            coEvery { countryRepository.existsByCode(code) } returns false
            coEvery { countryRepository.save(any()) } returns expectedResponse
            
            val result = countryService.createCountry(createDto)
            
            assertEquals(expectedResponse, result)
            coVerify { countryRepository.existsByCode(code) }
            coVerify { countryRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        
        @Test
        @DisplayName("should_update_existing_country_with_same_code")
        fun should_update_existing_country_with_same_code() = runTest {
            val existingCountry = CountryTestData.createCountryResponseDto()
            val updateDto = CountryTestData.createCountryCreateDto(code = existingCountry.code)
            val updatedCountry = existingCountry.copy(
                name = updateDto.name,
                updatedAt = Instant.now()
            )
            coEvery { countryRepository.findById(existingCountry.id) } returns existingCountry
            coEvery { countryRepository.save(any()) } returns updatedCountry
            
            val result = countryService.updateCountry(existingCountry.id, updateDto)
            
            assertEquals(updatedCountry, result)
            coVerify { countryRepository.findById(existingCountry.id) }
            coVerify { countryRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_update_existing_country_with_new_unique_code")
        fun should_update_existing_country_with_new_unique_code() = runTest {
            val existingCountry = CountryTestData.createCountryResponseDto()
            val updateDto = CountryTestData.createCountryCreateDto(code = "NEW")
            val updatedCountry = existingCountry.copy(
                name = updateDto.name,
                code = updateDto.code,
                updatedAt = Instant.now()
            )
            coEvery { countryRepository.findById(existingCountry.id) } returns existingCountry
            coEvery { countryRepository.existsByCode("NEW") } returns false
            coEvery { countryRepository.save(any()) } returns updatedCountry
            
            val result = countryService.updateCountry(existingCountry.id, updateDto)
            
            assertEquals(updatedCountry, result)
            coVerify { countryRepository.findById(existingCountry.id) }
            coVerify { countryRepository.existsByCode("NEW") }
            coVerify { countryRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_throw_exception_when_updating_with_existing_code")
        fun should_throw_exception_when_updating_with_existing_code() = runTest {
            val existingCountry = CountryTestData.createCountryResponseDto()
            val updateDto = CountryTestData.createCountryCreateDto(code = "EXISTING")
            coEvery { countryRepository.findById(existingCountry.id) } returns existingCountry
            coEvery { countryRepository.existsByCode("EXISTING") } returns true
            
            assertThrows<IllegalArgumentException> {
                countryService.updateCountry(existingCountry.id, updateDto)
            }
            
            coVerify { countryRepository.findById(existingCountry.id) }
            coVerify { countryRepository.existsByCode("EXISTING") }
            coVerify(exactly = 0) { countryRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_return_null_when_updating_non_existent_country")
        fun should_return_null_when_updating_non_existent_country() = runTest {
            val id = UUID.randomUUID()
            val updateDto = CountryTestData.createCountryCreateDto()
            coEvery { countryRepository.findById(id) } returns null
            
            val result = countryService.updateCountry(id, updateDto)
            
            assertNull(result)
            coVerify { countryRepository.findById(id) }
            coVerify(exactly = 0) { countryRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        
        @Test
        @DisplayName("should_return_true_when_country_exists_and_is_deleted")
        fun should_return_true_when_country_exists_and_is_deleted() = runTest {
            val id = UUID.randomUUID()
            coEvery { countryRepository.deleteById(id) } returns true
            
            val result = countryService.deleteCountry(id)
            
            assertTrue(result)
            coVerify { countryRepository.deleteById(id) }
        }
        
        @Test
        @DisplayName("should_return_false_when_country_does_not_exist")
        fun should_return_false_when_country_does_not_exist() = runTest {
            val id = UUID.randomUUID()
            coEvery { countryRepository.deleteById(id) } returns false
            
            val result = countryService.deleteCountry(id)
            
            assertFalse(result)
            coVerify { countryRepository.deleteById(id) }
        }
    }
}