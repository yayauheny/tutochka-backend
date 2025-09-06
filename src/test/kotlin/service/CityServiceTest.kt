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
import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.testdata.CityTestData
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

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
        fun should_retrieve_all_cities() = runTest {
            val cities = CityTestData.createCityList(3)
            coEvery { cityRepository.findAll() } returns cities
            
            val result = cityService.getAllCities()
            
            assertEquals(cities, result)
            coVerify { cityRepository.findAll() }
        }
        
        @Test
        @DisplayName("should_return_city_when_found_by_id")
        fun should_return_city_when_found_by_id() = runTest {
            val city = CityTestData.createCityResponseDto()
            coEvery { cityRepository.findById(city.id) } returns city
            
            val result = cityService.getCityById(city.id)
            
            assertEquals(city, result)
            coVerify { cityRepository.findById(city.id) }
        }
        
        @Test
        @DisplayName("should_return_null_when_city_not_found_by_id")
        fun should_return_null_when_city_not_found_by_id() = runTest {
            val id = UUID.randomUUID()
            coEvery { cityRepository.findById(id) } returns null
            
            val result = cityService.getCityById(id)
            
            assertNull(result)
            coVerify { cityRepository.findById(id) }
        }
        
        @Test
        @DisplayName("should_retrieve_cities_by_country_id")
        fun should_retrieve_cities_by_country_id() = runTest {
            val countryId = UUID.randomUUID()
            val cities = CityTestData.createCityList(2)
            coEvery { cityRepository.findByCountryId(countryId) } returns cities
            
            val result = cityService.getCitiesByCountry(countryId)
            
            assertEquals(cities, result)
            coVerify { cityRepository.findByCountryId(countryId) }
        }
        
        @Test
        @DisplayName("should_search_cities_by_name")
        fun should_search_cities_by_name() = runTest {
            val searchName = "New York"
            val cities = CityTestData.createCityList(2)
            coEvery { cityRepository.findByName(searchName) } returns cities
            
            val result = cityService.searchCitiesByName(searchName)
            
            assertEquals(cities, result)
            coVerify { cityRepository.findByName(searchName) }
        }
    }
    
    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        
        @Test
        @DisplayName("should_create_new_city_with_valid_country")
        fun should_create_new_city_with_valid_country() = runTest {
            val country = CityTestData.createCountryResponseDto()
            val createDto = CityTestData.createCityCreateDto(countryId = country.id)
            val expectedResponse = CityTestData.createCityResponseDto(countryId = country.id)
            coEvery { countryRepository.findById(country.id) } returns country
            coEvery { cityRepository.existsByCountryAndName(country.id, createDto.nameRu) } returns false
            coEvery { cityRepository.existsByCountryAndName(country.id, createDto.nameEn) } returns false
            coEvery { cityRepository.save(any()) } returns expectedResponse
            
            val result = cityService.createCity(createDto)
            
            assertEquals(expectedResponse, result)
            coVerify { countryRepository.findById(country.id) }
            coVerify { cityRepository.existsByCountryAndName(country.id, createDto.nameRu) }
            coVerify { cityRepository.existsByCountryAndName(country.id, createDto.nameEn) }
            coVerify { cityRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_throw_exception_when_country_not_found")
        fun should_throw_exception_when_country_not_found() = runTest {
            val countryId = UUID.randomUUID()
            val createDto = CityTestData.createCityCreateDto(countryId = countryId)
            coEvery { countryRepository.findById(countryId) } returns null
            
            assertThrows<IllegalArgumentException> {
                cityService.createCity(createDto)
            }
            
            coVerify { countryRepository.findById(countryId) }
            coVerify(exactly = 0) { cityRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_throw_exception_when_city_name_already_exists_in_country")
        fun should_throw_exception_when_city_name_already_exists_in_country() = runTest {
            val country = CityTestData.createCountryResponseDto()
            val createDto = CityTestData.createCityCreateDto(countryId = country.id)
            coEvery { countryRepository.findById(country.id) } returns country
            coEvery { cityRepository.existsByCountryAndName(country.id, createDto.nameRu) } returns true
            
            assertThrows<IllegalArgumentException> {
                cityService.createCity(createDto)
            }
            
            coVerify { countryRepository.findById(country.id) }
            coVerify { cityRepository.existsByCountryAndName(country.id, createDto.nameRu) }
            coVerify(exactly = 0) { cityRepository.save(any()) }
        }
        
        @ParameterizedTest
        @ValueSource(doubles = [40.7128, 51.5074, 48.8566, 35.6762, 55.7558])
        @DisplayName("should_handle_different_latitude_values")
        fun should_handle_different_latitude_values(lat: Double) = runTest {
            val country = CityTestData.createCountryResponseDto()
            val createDto = CityTestData.createCityCreateDto(
                countryId = country.id,
                lat = lat
            )
            val expectedResponse = CityTestData.createCityResponseDto(
                countryId = country.id,
                lat = lat
            )
            coEvery { countryRepository.findById(country.id) } returns country
            coEvery { cityRepository.existsByCountryAndName(country.id, createDto.nameRu) } returns false
            coEvery { cityRepository.existsByCountryAndName(country.id, createDto.nameEn) } returns false
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
        fun should_update_existing_city() = runTest {
            val country = CityTestData.createCountryResponseDto()
            val existingCity = CityTestData.createCityResponseDto(countryId = country.id)
            val updateDto = CityTestData.createCityCreateDto(countryId = country.id)
            val updatedCity = existingCity.copy(
                nameRu = updateDto.nameRu,
                nameEn = updateDto.nameEn,
                updatedAt = Instant.now()
            )
            coEvery { cityRepository.findById(existingCity.id) } returns existingCity
            coEvery { countryRepository.findById(country.id) } returns country
            coEvery { cityRepository.existsByCountryAndName(country.id, updateDto.nameRu) } returns false
            coEvery { cityRepository.existsByCountryAndName(country.id, updateDto.nameEn) } returns false
            coEvery { cityRepository.save(any()) } returns updatedCity
            
            val result = cityService.updateCity(existingCity.id, updateDto)
            
            assertEquals(updatedCity, result)
            coVerify { cityRepository.findById(existingCity.id) }
            coVerify { countryRepository.findById(country.id) }
            coVerify { cityRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_throw_exception_when_updating_with_non_existent_country")
        fun should_throw_exception_when_updating_with_non_existent_country() = runTest {
            val existingCity = CityTestData.createCityResponseDto()
            val updateDto = CityTestData.createCityCreateDto()
            coEvery { cityRepository.findById(existingCity.id) } returns existingCity
            coEvery { countryRepository.findById(updateDto.countryId) } returns null
            
            assertThrows<IllegalArgumentException> {
                cityService.updateCity(existingCity.id, updateDto)
            }
            
            coVerify { cityRepository.findById(existingCity.id) }
            coVerify { countryRepository.findById(updateDto.countryId) }
            coVerify(exactly = 0) { cityRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_return_null_when_updating_non_existent_city")
        fun should_return_null_when_updating_non_existent_city() = runTest {
            val id = UUID.randomUUID()
            val updateDto = CityTestData.createCityCreateDto()
            coEvery { cityRepository.findById(id) } returns null
            
            val result = cityService.updateCity(id, updateDto)
            
            assertNull(result)
            coVerify { cityRepository.findById(id) }
            coVerify(exactly = 0) { cityRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        
        @Test
        @DisplayName("should_return_true_when_city_exists_and_is_deleted")
        fun should_return_true_when_city_exists_and_is_deleted() = runTest {
            val id = UUID.randomUUID()
            coEvery { cityRepository.deleteById(id) } returns true
            
            val result = cityService.deleteCity(id)
            
            assertTrue(result)
            coVerify { cityRepository.deleteById(id) }
        }
        
        @Test
        @DisplayName("should_return_false_when_city_does_not_exist")
        fun should_return_false_when_city_does_not_exist() = runTest {
            val id = UUID.randomUUID()
            coEvery { cityRepository.deleteById(id) } returns false
            
            val result = cityService.deleteCity(id)
            
            assertFalse(result)
            coVerify { cityRepository.deleteById(id) }
        }
    }
}