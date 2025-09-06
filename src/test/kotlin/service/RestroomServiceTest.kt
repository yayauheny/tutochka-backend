package yayauheny.by.service

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.testdata.RestroomTestData
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("RestroomService Tests")
class RestroomServiceTest {
    
    private val restroomRepository = mockk<RestroomRepository>()
    private val restroomService = RestroomService(restroomRepository)
    
    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        
        @Test
        @DisplayName("should_retrieve_all_restrooms")
        fun should_retrieve_all_restrooms() = runTest {
            val restrooms = RestroomTestData.createRestroomList(3)
            coEvery { restroomRepository.findAll() } returns restrooms
            
            val result = restroomService.getAllRestrooms()
            
            assertEquals(restrooms, result)
            coVerify { restroomRepository.findAll() }
        }
        
        @Test
        @DisplayName("should_return_restroom_when_found_by_id")
        fun should_return_restroom_when_found_by_id() = runTest {
            val restroom = RestroomTestData.createRestroomResponseDto()
            coEvery { restroomRepository.findById(restroom.id) } returns restroom
            
            val result = restroomService.getRestroomById(restroom.id)
            
            assertEquals(restroom, result)
            coVerify { restroomRepository.findById(restroom.id) }
        }
        
        @Test
        @DisplayName("should_return_null_when_restroom_not_found_by_id")
        fun should_return_null_when_restroom_not_found_by_id() = runTest {
            val id = UUID.randomUUID()
            coEvery { restroomRepository.findById(id) } returns null
            
            val result = restroomService.getRestroomById(id)
            
            assertNull(result)
            coVerify { restroomRepository.findById(id) }
        }
        
        @Test
        @DisplayName("should_retrieve_restrooms_by_city_id")
        fun should_retrieve_restrooms_by_city_id() = runTest {
            val cityId = UUID.randomUUID()
            val restrooms = RestroomTestData.createRestroomList(2)
            coEvery { restroomRepository.findByCityId(cityId) } returns restrooms
            
            val result = restroomService.getRestroomsByCity(cityId)
            
            assertEquals(restrooms, result)
            coVerify { restroomRepository.findByCityId(cityId) }
        }
        
        @ParameterizedTest
        @ValueSource(ints = [1, 3, 5, 10])
        @DisplayName("should_find_nearest_restrooms_with_different_limits")
        fun should_find_nearest_restrooms_with_different_limits(limit: Int) = runTest {
            val latitude = 40.7829
            val longitude = -73.9654
            val restrooms = RestroomTestData.createRestroomList(limit)
            coEvery { restroomRepository.findNearestByLocation(latitude, longitude, limit) } returns restrooms
            
            val result = restroomService.findNearestRestrooms(latitude, longitude, limit)
            
            assertEquals(restrooms, result)
            coVerify { restroomRepository.findNearestByLocation(latitude, longitude, limit) }
        }
        
        @Test
        @DisplayName("should_use_default_limit_of_5_for_nearest_restrooms")
        fun should_use_default_limit_of_5_for_nearest_restrooms() = runTest {
            val latitude = 40.7829
            val longitude = -73.9654
            val restrooms = RestroomTestData.createRestroomList(5)
            coEvery { restroomRepository.findNearestByLocation(latitude, longitude, 5) } returns restrooms
            
            val result = restroomService.findNearestRestrooms(latitude, longitude)
            
            assertEquals(restrooms, result)
            coVerify { restroomRepository.findNearestByLocation(latitude, longitude, 5) }
        }
    }
    
    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        
        @Test
        @DisplayName("should_create_new_restroom_with_generated_id_and_timestamps")
        fun should_create_new_restroom_with_generated_id_and_timestamps() = runTest {
            val createDto = RestroomTestData.createRestroomCreateDto()
            val expectedResponse = RestroomTestData.createRestroomResponseDto()
            coEvery { restroomRepository.save(any()) } returns expectedResponse
            
            val result = restroomService.createRestroom(createDto)
            
            assertEquals(expectedResponse, result)
            coVerify { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_handle_different_amenity_configurations")
        fun should_handle_different_amenity_configurations() = runTest {
            val amenityConfigurations = RestroomTestData.createAmenityConfigurations()
            
            amenityConfigurations.forEach { (createDto, expectedResponse) ->
                coEvery { restroomRepository.save(any()) } returns expectedResponse
                
                val result = restroomService.createRestroom(createDto)
                
                assertEquals(expectedResponse, result)
            }
            
            coVerify(exactly = amenityConfigurations.size) { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_handle_different_accessibility_types")
        fun should_handle_different_accessibility_types() = runTest {
            val accessibilityTypes = AccessibilityType.values()
            
            accessibilityTypes.forEach { accessibilityType ->
                val createDto = RestroomTestData.createRestroomCreateDto(
                    accessibilityType = accessibilityType
                )
                val expectedResponse = RestroomTestData.createRestroomResponseDto(
                    accessibilityType = accessibilityType
                )
                coEvery { restroomRepository.save(any()) } returns expectedResponse
                
                val result = restroomService.createRestroom(createDto)
                
                assertEquals(expectedResponse, result)
            }
            
            coVerify(exactly = accessibilityTypes.size) { restroomRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        
        @Test
        @DisplayName("should_update_existing_restroom")
        fun should_update_existing_restroom() = runTest {
            val existingRestroom = RestroomTestData.createRestroomResponseDto()
            val updateDto = RestroomTestData.createRestroomCreateDto()
            val updatedRestroom = existingRestroom.copy(
                name = updateDto.name,
                updatedAt = Instant.now()
            )
            coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
            coEvery { restroomRepository.save(any()) } returns updatedRestroom
            
            val result = restroomService.updateRestroom(existingRestroom.id, updateDto)
            
            assertEquals(updatedRestroom, result)
            coVerify { restroomRepository.findById(existingRestroom.id) }
            coVerify { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_return_null_when_updating_non_existent_restroom")
        fun should_return_null_when_updating_non_existent_restroom() = runTest {
            val id = UUID.randomUUID()
            val updateDto = RestroomTestData.createRestroomCreateDto()
            coEvery { restroomRepository.findById(id) } returns null
            
            val result = restroomService.updateRestroom(id, updateDto)
            
            assertNull(result)
            coVerify { restroomRepository.findById(id) }
            coVerify(exactly = 0) { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("should_preserve_original_timestamps_when_updating")
        fun should_preserve_original_timestamps_when_updating() = runTest {
            val originalCreatedAt = Instant.parse("2023-01-01T00:00:00Z")
            val originalUpdatedAt = Instant.parse("2023-01-02T00:00:00Z")
            val existingRestroom = RestroomTestData.createRestroomResponseDto(
                createdAt = originalCreatedAt,
                updatedAt = originalUpdatedAt
            )
            val updateDto = RestroomTestData.createRestroomCreateDto()
            coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
            coEvery { restroomRepository.save(any()) } returns existingRestroom
            
            val result = restroomService.updateRestroom(existingRestroom.id, updateDto)
            
            assertEquals(existingRestroom, result)
            coVerify { restroomRepository.findById(existingRestroom.id) }
            coVerify { restroomRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        
        @Test
        @DisplayName("should_return_true_when_restroom_exists_and_is_deleted")
        fun should_return_true_when_restroom_exists_and_is_deleted() = runTest {
            val id = UUID.randomUUID()
            coEvery { restroomRepository.deleteById(id) } returns true
            
            val result = restroomService.deleteRestroom(id)
            
            assertTrue(result)
            coVerify { restroomRepository.deleteById(id) }
        }
        
        @Test
        @DisplayName("should_return_false_when_restroom_does_not_exist")
        fun should_return_false_when_restroom_does_not_exist() = runTest {
            val id = UUID.randomUUID()
            coEvery { restroomRepository.deleteById(id) } returns false
            
            val result = restroomService.deleteRestroom(id)
            
            assertFalse(result)
            coVerify { restroomRepository.deleteById(id) }
        }
    }
}