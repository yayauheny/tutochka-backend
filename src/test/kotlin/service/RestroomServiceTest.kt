package yayauheny.by.service

import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import yayauheny.by.enums.AccessibilityType
import yayauheny.by.enums.DataSourceType
import yayauheny.by.enums.FeeType
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.testdata.RestroomTestData

@DisplayName("RestroomService Tests")
class RestroomServiceTest {
    
    private val restroomRepository = mockk<RestroomRepository>(relaxed = true)
    private val restroomService = RestroomService(restroomRepository)
    
    @BeforeEach
    fun setUp() {
        clearMocks(restroomRepository)
    }
    
    private fun createBasicAmenities() = RestroomTestData.createBasicAmenities()
    
    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        
        @Test
        @DisplayName("GIVEN existing restrooms WHEN retrieving all THEN return paginated restrooms")
        fun given_existing_restrooms_when_retrieving_all_then_return_paginated_restrooms() = runTest {
            // GIVEN
            val pagination = PaginationDto(page = 0, size = 10)
            val expectedPage = PageResponseDto(
                content = RestroomTestData.createRestroomList(3),
                page = 0,
                size = 10,
                totalElements = 3L,
                totalPages = 1,
                first = true,
                last = true
            )
            coEvery { restroomRepository.findAll(pagination) } returns expectedPage
            
            // WHEN
            val actualPage = restroomService.getAllRestrooms(pagination)
            
            // THEN
            assertEquals(expectedPage, actualPage)
            coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
        }
        
        @Test
        @DisplayName("GIVEN existing restroom WHEN finding by ID THEN return restroom")
        fun given_existing_restroom_when_finding_by_id_then_return_restroom() = runTest {
            // GIVEN
            val expectedRestroom = RestroomTestData.createRestroomResponseDto()
            coEvery { restroomRepository.findById(expectedRestroom.id) } returns expectedRestroom
            
            // WHEN
            val actualRestroom = restroomService.getRestroomById(expectedRestroom.id)
            
            // THEN
            assertEquals(expectedRestroom, actualRestroom)
            coVerify(exactly = 1) { restroomRepository.findById(expectedRestroom.id) }
        }
        
        @Test
        @DisplayName("GIVEN non-existent restroom WHEN finding by ID THEN return null")
        fun given_non_existent_restroom_when_finding_by_id_then_return_null() = runTest {
            // GIVEN
            val nonExistentId = UUID.randomUUID()
            coEvery { restroomRepository.findById(nonExistentId) } returns null
            
            // WHEN
            val actualRestroom = restroomService.getRestroomById(nonExistentId)
            
            // THEN
            assertNull(actualRestroom)
            coVerify(exactly = 1) { restroomRepository.findById(nonExistentId) }
        }
        
        @Test
        @DisplayName("GIVEN restrooms in city WHEN finding by city ID THEN return city restrooms")
        fun given_restrooms_in_city_when_finding_by_city_id_then_return_city_restrooms() = runTest {
            // GIVEN
            val cityId = UUID.randomUUID()
            val pagination = PaginationDto(page = 0, size = 10)
            val expectedPage = PageResponseDto(
                content = RestroomTestData.createRestroomList(2),
                page = 0,
                size = 10,
                totalElements = 2L,
                totalPages = 1,
                first = true,
                last = true
            )
            coEvery { restroomRepository.findByCityId(cityId, pagination) } returns expectedPage
            
            // WHEN
            val actualPage = restroomService.getRestroomsByCity(cityId, pagination)
            
            // THEN
            assertEquals(expectedPage, actualPage)
            coVerify(exactly = 1) { restroomRepository.findByCityId(cityId, pagination) }
        }
        
        @ParameterizedTest
        @ValueSource(ints = [1, 3, 5, 10, 20])
        @DisplayName("GIVEN location and limit WHEN finding nearest restrooms THEN return limited results")
        fun given_location_and_limit_when_finding_nearest_restrooms_then_return_limited_results(limit: Int) = runTest {
            // GIVEN
            val latitude = 40.7829
            val longitude = -73.9654
            val expectedRestrooms = RestroomTestData.createRestroomList(limit)
            coEvery { restroomRepository.findNearestByLocation(latitude, longitude, limit) } returns expectedRestrooms
            
            // WHEN
            val actualRestrooms = restroomService.findNearestRestrooms(latitude, longitude, limit)
            
            // THEN
            assertEquals(expectedRestrooms, actualRestrooms)
            assertEquals(limit, actualRestrooms.size)
            coVerify(exactly = 1) { restroomRepository.findNearestByLocation(latitude, longitude, limit) }
        }
        
        @Test
        @DisplayName("GIVEN location without limit WHEN finding nearest restrooms THEN use default limit of 5")
        fun given_location_without_limit_when_finding_nearest_restrooms_then_use_default_limit_of_5() = runTest {
            // GIVEN
            val latitude = 40.7829
            val longitude = -73.9654
            val expectedRestrooms = RestroomTestData.createRestroomList(5)
            coEvery { restroomRepository.findNearestByLocation(latitude, longitude, 5) } returns expectedRestrooms
            
            // WHEN
            val actualRestrooms = restroomService.findNearestRestrooms(latitude, longitude)
            
            // THEN
            assertEquals(expectedRestrooms, actualRestrooms)
            assertEquals(5, actualRestrooms.size)
            coVerify(exactly = 1) { restroomRepository.findNearestByLocation(latitude, longitude, 5) }
        }
        
        @ParameterizedTest
        @CsvSource(
            "0, 0, 0",
            "90, 180, 1", 
            "-90, -180, 1",
            "45.5, -73.5, 1"
        )
        @DisplayName("GIVEN edge case coordinates WHEN finding nearest restrooms THEN handle correctly")
        fun given_edge_case_coordinates_when_finding_nearest_restrooms_then_handle_correctly(
            lat: Double, lon: Double, expectedCount: Int
        ) = runTest {
            // GIVEN
            val expectedRestrooms = RestroomTestData.createRestroomList(expectedCount)
            coEvery { restroomRepository.findNearestByLocation(lat, lon, 5) } returns expectedRestrooms
            
            // WHEN
            val actualRestrooms = restroomService.findNearestRestrooms(lat, lon)
            
            // THEN
            assertEquals(expectedRestrooms, actualRestrooms)
            coVerify(exactly = 1) { restroomRepository.findNearestByLocation(lat, lon, 5) }
        }
    }
    
    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        
        @Test
        @DisplayName("GIVEN valid restroom data WHEN creating restroom THEN return created restroom with ACTIVE status")
        fun given_valid_restroom_data_when_creating_restroom_then_return_created_restroom_with_active_status() = runTest {
            // GIVEN
            val createDto = RestroomTestData.createRestroomCreateDto()
            val expectedResponse = RestroomTestData.createRestroomResponseDto(status = RestroomStatus.ACTIVE)
            val savedRestroomSlot = slot<RestroomResponseDto>()
            coEvery { restroomRepository.save(capture(savedRestroomSlot)) } returns expectedResponse
            
            // WHEN
            val actualResponse = restroomService.createRestroom(createDto)
            
            // THEN
            assertEquals(expectedResponse, actualResponse)
            assertEquals(RestroomStatus.ACTIVE, savedRestroomSlot.captured.status)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @ParameterizedTest
        @EnumSource(AccessibilityType::class)
        @DisplayName("GIVEN different accessibility types WHEN creating restroom THEN handle all types correctly")
        fun given_different_accessibility_types_when_creating_restroom_then_handle_all_types_correctly(
            accessibilityType: AccessibilityType
        ) = runTest {
            // GIVEN
            val createDto = RestroomTestData.createRestroomCreateDto(accessibilityType = accessibilityType)
            val expectedResponse = RestroomTestData.createRestroomResponseDto(accessibilityType = accessibilityType)
            coEvery { restroomRepository.save(any()) } returns expectedResponse
            
            // WHEN
            val actualResponse = restroomService.createRestroom(createDto)
            
            // THEN
            assertEquals(expectedResponse, actualResponse)
            assertEquals(accessibilityType, actualResponse.accessibilityType)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @ParameterizedTest
        @EnumSource(FeeType::class)
        @DisplayName("GIVEN different fee types WHEN creating restroom THEN handle all types correctly")
        fun given_different_fee_types_when_creating_restroom_then_handle_all_types_correctly(
            feeType: FeeType
        ) = runTest {
            // GIVEN
            val createDto = RestroomTestData.createRestroomCreateDto(feeType = feeType)
            val expectedResponse = RestroomTestData.createRestroomResponseDto(feeType = feeType)
            coEvery { restroomRepository.save(any()) } returns expectedResponse
            
            // WHEN
            val actualResponse = restroomService.createRestroom(createDto)
            
            // THEN
            assertEquals(expectedResponse, actualResponse)
            assertEquals(feeType, actualResponse.feeType)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @ParameterizedTest
        @EnumSource(DataSourceType::class)
        @DisplayName("GIVEN different data sources WHEN creating restroom THEN handle all types correctly")
        fun given_different_data_sources_when_creating_restroom_then_handle_all_types_correctly(
            dataSource: DataSourceType
        ) = runTest {
            // GIVEN
            val createDto = RestroomTestData.createRestroomCreateDto(dataSource = dataSource)
            val expectedResponse = RestroomTestData.createRestroomResponseDto(dataSource = dataSource)
            coEvery { restroomRepository.save(any()) } returns expectedResponse
            
            // WHEN
            val actualResponse = restroomService.createRestroom(createDto)
            
            // THEN
            assertEquals(expectedResponse, actualResponse)
            assertEquals(dataSource, actualResponse.dataSource)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("GIVEN restroom with null city ID WHEN creating restroom THEN handle correctly")
        fun given_restroom_with_null_city_id_when_creating_restroom_then_handle_correctly() = runTest {
            // GIVEN
            val createDto = RestroomCreateDto(
                cityId = null,
                name = "Test Restroom",
                description = "Test Description",
                address = "Test Address",
                phones = null,
                workTime = null,
                feeType = FeeType.FREE,
                accessibilityType = AccessibilityType.UNISEX,
                lat = 40.7829,
                lon = -73.9654,
                dataSource = DataSourceType.MANUAL,
                amenities = createBasicAmenities()
            )
            val expectedResponse = RestroomResponseDto(
                id = UUID.randomUUID(),
                cityId = null,
                name = "Test Restroom",
                description = "Test Description",
                address = "Test Address",
                phones = null,
                workTime = null,
                feeType = FeeType.FREE,
                accessibilityType = AccessibilityType.UNISEX,
                lat = 40.7829,
                lon = -73.9654,
                dataSource = DataSourceType.MANUAL,
                status = RestroomStatus.ACTIVE,
                amenities = createBasicAmenities(),
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
            coEvery { restroomRepository.save(any()) } returns expectedResponse
            
            // WHEN
            val actualResponse = restroomService.createRestroom(createDto)
            
            // THEN
            assertEquals(expectedResponse, actualResponse)
            assertNull(actualResponse.cityId)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Update Operations")
    inner class UpdateOperations {
        
        @Test
        @DisplayName("GIVEN existing restroom WHEN updating THEN return updated restroom")
        fun given_existing_restroom_when_updating_then_return_updated_restroom() = runTest {
            // GIVEN
            val existingRestroom = RestroomTestData.createRestroomResponseDto()
            val updateDto = RestroomTestData.createRestroomCreateDto()
            val updatedRestroom = existingRestroom.copy(
                name = updateDto.name,
                description = updateDto.description,
                address = updateDto.address,
                updatedAt = Instant.now()
            )
            coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
            coEvery { restroomRepository.save(any()) } returns updatedRestroom
            
            // WHEN
            val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)
            
            // THEN
            assertEquals(updatedRestroom, actualResult)
            coVerify(exactly = 1) { restroomRepository.findById(existingRestroom.id) }
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("GIVEN non-existent restroom WHEN updating THEN return null")
        fun given_non_existent_restroom_when_updating_then_return_null() = runTest {
            // GIVEN
            val nonExistentId = UUID.randomUUID()
            val updateDto = RestroomTestData.createRestroomCreateDto()
            coEvery { restroomRepository.findById(nonExistentId) } returns null
            
            // WHEN
            val actualResult = restroomService.updateRestroom(nonExistentId, updateDto)
            
            // THEN
            assertNull(actualResult)
            coVerify(exactly = 1) { restroomRepository.findById(nonExistentId) }
            coVerify(exactly = 0) { restroomRepository.save(any()) }
        }
        
        @Test
        @DisplayName("GIVEN existing restroom WHEN updating with same data THEN preserve original timestamps")
        fun given_existing_restroom_when_updating_with_same_data_then_preserve_original_timestamps() = runTest {
            // GIVEN
            val originalCreatedAt = Instant.parse("2023-01-01T00:00:00Z")
            val originalUpdatedAt = Instant.parse("2023-01-02T00:00:00Z")
            val existingRestroom = RestroomTestData.createRestroomResponseDto(
                createdAt = originalCreatedAt,
                updatedAt = originalUpdatedAt
            )
            val updateDto = RestroomTestData.createRestroomCreateDto(
                name = existingRestroom.name ?: "Test Name",
                description = existingRestroom.description ?: "Test Description",
                address = existingRestroom.address
            )
            coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
            coEvery { restroomRepository.save(any()) } returns existingRestroom
            
            // WHEN
            val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)
            
            // THEN
            assertEquals(existingRestroom, actualResult)
            assertNotNull(actualResult)
            assertEquals(originalCreatedAt, actualResult!!.createdAt)
            coVerify(exactly = 1) { restroomRepository.findById(existingRestroom.id) }
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
        
        @ParameterizedTest
        @EnumSource(RestroomStatus::class)
        @DisplayName("GIVEN existing restroom with different status WHEN updating THEN preserve status")
        fun given_existing_restroom_with_different_status_when_updating_then_preserve_status(
            status: RestroomStatus
        ) = runTest {
            // GIVEN
            val existingRestroom = RestroomTestData.createRestroomResponseDto(status = status)
            val updateDto = RestroomTestData.createRestroomCreateDto()
            coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
            coEvery { restroomRepository.save(any()) } returns existingRestroom
            
            // WHEN
            val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)
            
            // THEN
            assertEquals(existingRestroom, actualResult)
            assertNotNull(actualResult)
            assertEquals(status, actualResult!!.status)
            coVerify(exactly = 1) { restroomRepository.findById(existingRestroom.id) }
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }
    }
    
    @Nested
    @DisplayName("Delete Operations")
    inner class DeleteOperations {
        
        @Test
        @DisplayName("GIVEN existing restroom WHEN deleting THEN return true")
        fun given_existing_restroom_when_deleting_then_return_true() = runTest {
            // GIVEN
            val restroomId = UUID.randomUUID()
            coEvery { restroomRepository.deleteById(restroomId) } returns true
            
            // WHEN
            val actualResult = restroomService.deleteRestroom(restroomId)
            
            // THEN
            assertTrue(actualResult)
            coVerify(exactly = 1) { restroomRepository.deleteById(restroomId) }
        }
        
        @Test
        @DisplayName("GIVEN non-existent restroom WHEN deleting THEN return false")
        fun given_non_existent_restroom_when_deleting_then_return_false() = runTest {
            // GIVEN
            val nonExistentId = UUID.randomUUID()
            coEvery { restroomRepository.deleteById(nonExistentId) } returns false
            
            // WHEN
            val actualResult = restroomService.deleteRestroom(nonExistentId)
            
            // THEN
            assertFalse(actualResult)
            coVerify(exactly = 1) { restroomRepository.deleteById(nonExistentId) }
        }
    }
    
    @Nested
    @DisplayName("Pagination Operations")
    inner class PaginationOperations {
        
        @Test
        @DisplayName("GIVEN restrooms and pagination WHEN getting all restrooms THEN return correct page")
        fun given_restrooms_and_pagination_when_getting_all_restrooms_then_return_correct_page() = runTest {
            // GIVEN
            val pagination = PaginationDto(page = 1, size = 3)
            val expectedPage = PageResponseDto(
                content = RestroomTestData.createRestroomList(3),
                page = 1,
                size = 3,
                totalElements = 10L,
                totalPages = 4,
                first = false,
                last = false
            )
            coEvery { restroomRepository.findAll(pagination) } returns expectedPage
            
            // WHEN
            val actualPage = restroomService.getAllRestrooms(pagination)
            
            // THEN
            assertEquals(expectedPage, actualPage)
            coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
        }
        
        @Test
        @DisplayName("GIVEN empty restrooms WHEN getting all restrooms THEN return empty page")
        fun given_empty_restrooms_when_getting_all_restrooms_then_return_empty_page() = runTest {
            // GIVEN
            val pagination = PaginationDto(page = 0, size = 10)
            val expectedPage = PageResponseDto(
                content = emptyList<RestroomResponseDto>(),
                page = 0,
                size = 10,
                totalElements = 0L,
                totalPages = 0,
                first = true,
                last = true
            )
            coEvery { restroomRepository.findAll(pagination) } returns expectedPage
            
            // WHEN
            val actualPage = restroomService.getAllRestrooms(pagination)
            
            // THEN
            assertEquals(expectedPage, actualPage)
            coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
        }
        
        @Test
        @DisplayName("GIVEN restrooms and city ID WHEN getting city restrooms THEN return correct page")
        fun given_restrooms_and_city_id_when_getting_city_restrooms_then_return_correct_page() = runTest {
            // GIVEN
            val cityId = UUID.randomUUID()
            val pagination = PaginationDto(page = 0, size = 5)
            val expectedPage = PageResponseDto(
                content = RestroomTestData.createRestroomList(5),
                page = 0,
                size = 5,
                totalElements = 7L,
                totalPages = 2,
                first = true,
                last = false
            )
            coEvery { restroomRepository.findByCityId(cityId, pagination) } returns expectedPage
            
            // WHEN
            val actualPage = restroomService.getRestroomsByCity(cityId, pagination)
            
            // THEN
            assertEquals(expectedPage, actualPage)
            coVerify(exactly = 1) { restroomRepository.findByCityId(cityId, pagination) }
        }
    }
}