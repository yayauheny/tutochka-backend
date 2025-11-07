package yayauheny.by.unit.service

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
import support.helpers.TestDataHelpers
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.service.RestroomService

@DisplayName("RestroomService Tests")
class RestroomServiceTest {
    private val restroomRepository = mockk<RestroomRepository>(relaxed = true)
    private val restroomService = RestroomService(restroomRepository)

    @BeforeEach
    fun setUp() {
        clearMocks(restroomRepository)
    }

    private fun createBasicAmenities() = TestDataHelpers.createBasicAmenities()

    @Nested
    @DisplayName("Find Operations")
    inner class FindOperations {
        @Test
        @DisplayName("GIVEN existing restrooms WHEN retrieving all THEN return paginated restrooms")
        fun given_existing_restrooms_when_retrieving_all_then_return_paginated_restrooms() =
            runTest {
                val pagination = PaginationDto(page = 0, size = 10)
                val expectedPage =
                    PageResponseDto(
                        content = TestDataHelpers.createRestroomList(3),
                        page = 0,
                        size = 10,
                        totalElements = 3L,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { restroomRepository.findAll(pagination) } returns expectedPage

                val actualPage = restroomService.getAllRestrooms(pagination)

                assertEquals(expectedPage, actualPage)
                coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("GIVEN existing restroom WHEN finding by ID THEN return restroom")
        fun given_existing_restroom_when_finding_by_id_then_return_restroom() =
            runTest {
                val expectedRestroom = TestDataHelpers.createRestroomResponseDto()
                coEvery { restroomRepository.findById(expectedRestroom.id) } returns expectedRestroom

                val actualRestroom = restroomService.getRestroomById(expectedRestroom.id)

                assertEquals(expectedRestroom, actualRestroom)
                coVerify(exactly = 1) { restroomRepository.findById(expectedRestroom.id) }
            }

        @Test
        @DisplayName("GIVEN non-existent restroom WHEN finding by ID THEN return null")
        fun given_non_existent_restroom_when_finding_by_id_then_return_null() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                coEvery { restroomRepository.findById(nonExistentId) } returns null

                val actualRestroom = restroomService.getRestroomById(nonExistentId)

                assertNull(actualRestroom)
                coVerify(exactly = 1) { restroomRepository.findById(nonExistentId) }
            }

        @Test
        @DisplayName("GIVEN restrooms in city WHEN finding by city ID THEN return city restrooms")
        fun given_restrooms_in_city_when_finding_by_city_id_then_return_city_restrooms() =
            runTest {
                val cityId = UUID.randomUUID()
                val pagination = PaginationDto(page = 0, size = 10)
                val expectedPage =
                    PageResponseDto(
                        content = TestDataHelpers.createRestroomList(2),
                        page = 0,
                        size = 10,
                        totalElements = 2L,
                        totalPages = 1,
                        first = true,
                        last = true
                    )
                coEvery { restroomRepository.findByCityId(cityId, pagination) } returns expectedPage

                val actualPage = restroomService.getRestroomsByCity(cityId, pagination)

                assertEquals(expectedPage, actualPage)
                coVerify(exactly = 1) { restroomRepository.findByCityId(cityId, pagination) }
            }

        @ParameterizedTest
        @ValueSource(ints = [1, 3, 5, 10, 20])
        @DisplayName("GIVEN location and limit WHEN finding nearest restrooms THEN return limited results with distance")
        fun given_location_and_limit_when_finding_nearest_restrooms_then_return_limited_results(limit: Int) =
            runTest {
                val latitude = 40.7829
                val longitude = -73.9654
                val expectedRestrooms: List<NearestRestroomResponseDto> =
                    TestDataHelpers.createNearestRestroomList(
                        limit
                    )
                coEvery { restroomRepository.findNearestByLocation(latitude, longitude, limit) } returns expectedRestrooms

                val actualRestrooms = restroomService.findNearestRestrooms(latitude, longitude, limit)

                assertEquals(expectedRestrooms, actualRestrooms)
                assertEquals(limit, actualRestrooms.size)
                actualRestrooms.forEach { restroom ->
                    assertTrue(restroom.distanceMeters >= 0, "Distance should be non-negative")
                }
                coVerify(exactly = 1) { restroomRepository.findNearestByLocation(latitude, longitude, limit) }
            }

        @Test
        @DisplayName("GIVEN location without limit WHEN finding nearest restrooms THEN use default limit of 5")
        fun given_location_without_limit_when_finding_nearest_restrooms_then_use_default_limit_of_5() =
            runTest {
                val latitude = 40.7829
                val longitude = -73.9654
                val expectedRestrooms: List<NearestRestroomResponseDto> = TestDataHelpers.createNearestRestroomList(5)
                coEvery { restroomRepository.findNearestByLocation(latitude, longitude, 5) } returns expectedRestrooms

                val actualRestrooms = restroomService.findNearestRestrooms(latitude, longitude)

                assertEquals(expectedRestrooms, actualRestrooms)
                assertEquals(5, actualRestrooms.size)
                actualRestrooms.forEach { restroom ->
                    assertTrue(restroom.distanceMeters >= 0, "Distance should be non-negative")
                }
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
            lat: Double,
            lon: Double,
            expectedCount: Int
        ) = runTest {
            val expectedRestrooms: List<NearestRestroomResponseDto> =
                TestDataHelpers.createNearestRestroomList(
                    expectedCount
                )
            coEvery { restroomRepository.findNearestByLocation(lat, lon, 5) } returns expectedRestrooms

            val actualRestrooms = restroomService.findNearestRestrooms(lat, lon)

            assertEquals(expectedRestrooms, actualRestrooms)
            actualRestrooms.forEach { restroom ->
                assertTrue(restroom.distanceMeters >= 0, "Distance should be non-negative")
            }
            coVerify(exactly = 1) { restroomRepository.findNearestByLocation(lat, lon, 5) }
        }
    }

    @Nested
    @DisplayName("Create Operations")
    inner class CreateOperations {
        @Test
        @DisplayName("GIVEN valid restroom data WHEN creating restroom THEN return created restroom with ACTIVE status")
        fun given_valid_restroom_data_when_creating_restroom_then_return_created_restroom_with_active_status() =
            runTest {
                val createDto = TestDataHelpers.createRestroomCreateDto()
                val expectedResponse = TestDataHelpers.createRestroomResponseDto(status = RestroomStatus.ACTIVE)
                val savedRestroomSlot = slot<RestroomResponseDto>()
                coEvery { restroomRepository.save(capture(savedRestroomSlot)) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

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
            val createDto = TestDataHelpers.createRestroomCreateDto(accessibilityType = accessibilityType)
            val expectedResponse = TestDataHelpers.createRestroomResponseDto(accessibilityType = accessibilityType)
            coEvery { restroomRepository.save(any()) } returns expectedResponse

            val actualResponse = restroomService.createRestroom(createDto)

            assertEquals(expectedResponse, actualResponse)
            assertEquals(accessibilityType, actualResponse.accessibilityType)
            coVerify(exactly = 1) { restroomRepository.save(any()) }
        }

        @ParameterizedTest
        @EnumSource(FeeType::class)
        @DisplayName("GIVEN different fee types WHEN creating restroom THEN handle all types correctly")
        fun given_different_fee_types_when_creating_restroom_then_handle_all_types_correctly(feeType: FeeType) =
            runTest {
                val createDto = TestDataHelpers.createRestroomCreateDto(feeType = feeType)
                val expectedResponse = TestDataHelpers.createRestroomResponseDto(feeType = feeType)
                coEvery { restroomRepository.save(any()) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

                assertEquals(expectedResponse, actualResponse)
                assertEquals(feeType, actualResponse.feeType)
                coVerify(exactly = 1) { restroomRepository.save(any()) }
            }

        @Test
        @DisplayName("GIVEN restroom with parent place data WHEN creating restroom THEN save parent place fields correctly")
        fun given_restroom_with_parent_place_data_when_creating_restroom_then_save_parent_place_fields_correctly() =
            runTest {
                val createDto =
                    TestDataHelpers.createRestroomCreateDto(
                        parentPlaceName = "Central Park Mall",
                        parentPlaceType = "SHOPPING_MALL",
                        inheritParentSchedule = true
                    )
                val expectedResponse =
                    TestDataHelpers.createRestroomResponseDto(
                        parentPlaceName = "Central Park Mall",
                        parentPlaceType = "SHOPPING_MALL",
                        inheritParentSchedule = true
                    )
                coEvery { restroomRepository.save(any()) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

                assertEquals(expectedResponse, actualResponse)
                assertEquals("Central Park Mall", actualResponse.parentPlaceName)
                assertEquals("SHOPPING_MALL", actualResponse.parentPlaceType)
                assertTrue(actualResponse.inheritParentSchedule)
                coVerify(exactly = 1) { restroomRepository.save(any()) }
            }

        @ParameterizedTest
        @EnumSource(DataSourceType::class)
        @DisplayName("GIVEN different data sources WHEN creating restroom THEN handle all types correctly")
        fun given_different_data_sources_when_creating_restroom_then_handle_all_types_correctly(dataSource: DataSourceType) =
            runTest {
                val createDto = TestDataHelpers.createRestroomCreateDto(dataSource = dataSource)
                val expectedResponse = TestDataHelpers.createRestroomResponseDto(dataSource = dataSource)
                coEvery { restroomRepository.save(any()) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

                assertEquals(expectedResponse, actualResponse)
                assertEquals(dataSource, actualResponse.dataSource)
                coVerify(exactly = 1) { restroomRepository.save(any()) }
            }

        @Test
        @DisplayName("GIVEN restroom with null city ID WHEN creating restroom THEN handle correctly")
        fun given_restroom_with_null_city_id_when_creating_restroom_then_handle_correctly() =
            runTest {
                val createDto =
                    RestroomCreateDto(
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
                        amenities = createBasicAmenities(),
                        parentPlaceName = null,
                        parentPlaceType = null,
                        inheritParentSchedule = false
                    )
                val expectedResponse =
                    RestroomResponseDto(
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
                        parentPlaceName = null,
                        parentPlaceType = null,
                        inheritParentSchedule = false,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                coEvery { restroomRepository.save(any()) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

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
        fun given_existing_restroom_when_updating_then_return_updated_restroom() =
            runTest {
                val existingRestroom = TestDataHelpers.createRestroomResponseDto()
                val updateDto = TestDataHelpers.createRestroomCreateDto()
                val updatedRestroom =
                    existingRestroom.copy(
                        name = updateDto.name,
                        description = updateDto.description,
                        address = updateDto.address,
                        updatedAt = Instant.now()
                    )
                coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
                coEvery { restroomRepository.save(any()) } returns updatedRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

                assertEquals(updatedRestroom, actualResult)
                coVerify(exactly = 1) { restroomRepository.findById(existingRestroom.id) }
                coVerify(exactly = 1) { restroomRepository.save(any()) }
            }

        @Test
        @DisplayName("GIVEN non-existent restroom WHEN updating THEN return null")
        fun given_non_existent_restroom_when_updating_then_return_null() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createRestroomCreateDto()
                coEvery { restroomRepository.findById(nonExistentId) } returns null

                val actualResult = restroomService.updateRestroom(nonExistentId, updateDto)

                assertNull(actualResult)
                coVerify(exactly = 1) { restroomRepository.findById(nonExistentId) }
                coVerify(exactly = 0) { restroomRepository.save(any()) }
            }

        @Test
        @DisplayName("GIVEN existing restroom WHEN updating with same data THEN preserve original timestamps")
        fun given_existing_restroom_when_updating_with_same_data_then_preserve_original_timestamps() =
            runTest {
                val originalCreatedAt = Instant.parse("2023-01-01T00:00:00Z")
                val originalUpdatedAt = Instant.parse("2023-01-02T00:00:00Z")
                val existingRestroom =
                    TestDataHelpers.createRestroomResponseDto(
                        createdAt = originalCreatedAt,
                        updatedAt = originalUpdatedAt
                    )
                val updateDto =
                    TestDataHelpers.createRestroomCreateDto(
                        name = existingRestroom.name ?: "Test Name",
                        description = existingRestroom.description ?: "Test Description",
                        address = existingRestroom.address
                    )
                coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
                coEvery { restroomRepository.save(any()) } returns existingRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

                assertEquals(existingRestroom, actualResult)
                assertNotNull(actualResult)
                assertEquals(originalCreatedAt, actualResult!!.createdAt)
                coVerify(exactly = 1) { restroomRepository.findById(existingRestroom.id) }
                coVerify(exactly = 1) { restroomRepository.save(any()) }
            }

        @ParameterizedTest
        @EnumSource(RestroomStatus::class)
        @DisplayName("GIVEN existing restroom with different status WHEN updating THEN preserve status")
        fun given_existing_restroom_with_different_status_when_updating_then_preserve_status(status: RestroomStatus) =
            runTest {
                val existingRestroom = TestDataHelpers.createRestroomResponseDto(status = status)
                val updateDto = TestDataHelpers.createRestroomCreateDto()
                coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
                coEvery { restroomRepository.save(any()) } returns existingRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

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
        fun given_existing_restroom_when_deleting_then_return_true() =
            runTest {
                val restroomId = UUID.randomUUID()
                coEvery { restroomRepository.deleteById(restroomId) } returns true

                val actualResult = restroomService.deleteRestroom(restroomId)

                assertTrue(actualResult)
                coVerify(exactly = 1) { restroomRepository.deleteById(restroomId) }
            }

        @Test
        @DisplayName("GIVEN non-existent restroom WHEN deleting THEN return false")
        fun given_non_existent_restroom_when_deleting_then_return_false() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                coEvery { restroomRepository.deleteById(nonExistentId) } returns false

                val actualResult = restroomService.deleteRestroom(nonExistentId)

                assertFalse(actualResult)
                coVerify(exactly = 1) { restroomRepository.deleteById(nonExistentId) }
            }
    }

    @Nested
    @DisplayName("Pagination Operations")
    inner class PaginationOperations {
        @Test
        @DisplayName("GIVEN restrooms and pagination WHEN getting all restrooms THEN return correct page")
        fun given_restrooms_and_pagination_when_getting_all_restrooms_then_return_correct_page() =
            runTest {
                val pagination = PaginationDto(page = 1, size = 3)
                val expectedPage =
                    PageResponseDto(
                        content = TestDataHelpers.createRestroomList(3),
                        page = 1,
                        size = 3,
                        totalElements = 10L,
                        totalPages = 4,
                        first = false,
                        last = false
                    )
                coEvery { restroomRepository.findAll(pagination) } returns expectedPage

                val actualPage = restroomService.getAllRestrooms(pagination)

                assertEquals(expectedPage, actualPage)
                coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("GIVEN empty restrooms WHEN getting all restrooms THEN return empty page")
        fun given_empty_restrooms_when_getting_all_restrooms_then_return_empty_page() =
            runTest {
                val pagination = PaginationDto(page = 0, size = 10)
                val expectedPage =
                    PageResponseDto(
                        content = emptyList<RestroomResponseDto>(),
                        page = 0,
                        size = 10,
                        totalElements = 0L,
                        totalPages = 0,
                        first = true,
                        last = true
                    )
                coEvery { restroomRepository.findAll(pagination) } returns expectedPage

                val actualPage = restroomService.getAllRestrooms(pagination)

                assertEquals(expectedPage, actualPage)
                coVerify(exactly = 1) { restroomRepository.findAll(pagination) }
            }

        @Test
        @DisplayName("GIVEN restrooms and city ID WHEN getting city restrooms THEN return correct page")
        fun given_restrooms_and_city_id_when_getting_city_restrooms_then_return_correct_page() =
            runTest {
                val cityId = UUID.randomUUID()
                val pagination = PaginationDto(page = 0, size = 5)
                val expectedPage =
                    PageResponseDto(
                        content = TestDataHelpers.createRestroomList(5),
                        page = 0,
                        size = 5,
                        totalElements = 7L,
                        totalPages = 2,
                        first = true,
                        last = false
                    )
                coEvery { restroomRepository.findByCityId(cityId, pagination) } returns expectedPage

                val actualPage = restroomService.getRestroomsByCity(cityId, pagination)

                assertEquals(expectedPage, actualPage)
                coVerify(exactly = 1) { restroomRepository.findByCityId(cityId, pagination) }
            }
    }
}
