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
import yayauheny.by.helpers.TestDataHelpers
import yayauheny.by.model.dto.NearestRestroomSlimDto
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.PageResponse
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.repository.RestroomRepository
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
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    PageResponse(
                        content = TestDataHelpers.createRestroomList(3),
                        page = 0,
                        size = 10,
                        totalElements = 3,
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
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    PageResponse(
                        content = TestDataHelpers.createRestroomList(2),
                        page = 0,
                        size = 10,
                        totalElements = 2,
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
                val expectedRestrooms: List<NearestRestroomSlimDto> =
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
                val expectedRestrooms: List<NearestRestroomSlimDto> = TestDataHelpers.createNearestRestroomList(5)
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
            val expectedRestrooms: List<NearestRestroomSlimDto> =
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
                val savedRestroomSlot = slot<RestroomCreateDto>()
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
                val buildingId = UUID.randomUUID()
                val createDto =
                    TestDataHelpers.createRestroomCreateDto(
                        buildingId = buildingId,
                        inheritBuildingSchedule = true
                    )
                val expectedResponse =
                    TestDataHelpers.createRestroomResponseDto(
                        buildingId = buildingId,
                        inheritBuildingSchedule = true
                    )
                coEvery { restroomRepository.save(any()) } returns expectedResponse

                val actualResponse = restroomService.createRestroom(createDto)

                assertEquals(expectedResponse, actualResponse)
                assertNotNull(actualResponse.buildingId)
                assertEquals(true, actualResponse.inheritBuildingSchedule)
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
                    TestDataHelpers
                        .createRestroomCreateDto(
                            name = "Test Restroom",
                            address = "Test Address",
                            phones = null,
                            workTime = null,
                            lat = 40.7829,
                            lon = -73.9654
                        ).copy(cityId = null)
                val expectedResponse =
                    TestDataHelpers
                        .createRestroomResponseDto(
                            id = UUID.randomUUID(),
                            name = "Test Restroom",
                            address = "Test Address",
                            phones = null,
                            workTime = null,
                            lat = 40.7829,
                            lon = -73.9654,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now()
                        ).copy(cityId = null)
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
                val updateDto = TestDataHelpers.createRestroomUpdateDto()
                val updatedRestroom =
                    existingRestroom.copy(
                        name = updateDto.name ?: existingRestroom.name,
                        accessNote = updateDto.accessNote ?: existingRestroom.accessNote,
                        address = updateDto.address,
                        updatedAt = Instant.now()
                    )
                coEvery { restroomRepository.findById(existingRestroom.id) } returns existingRestroom
                coEvery { restroomRepository.update(any(), any()) } returns updatedRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

                assertEquals(updatedRestroom, actualResult)
                coVerify(exactly = 1) { restroomRepository.update(any(), any()) }
            }

        @Test
        @DisplayName("GIVEN non-existent restroom WHEN updating THEN throw error")
        fun given_non_existent_restroom_when_updating_then_throw_error() =
            runTest {
                val nonExistentId = UUID.randomUUID()
                val updateDto = TestDataHelpers.createRestroomUpdateDto()
                coEvery { restroomRepository.update(nonExistentId, updateDto) } throws Exception("Failed to update restroom $nonExistentId")

                val exception =
                    kotlin.test.assertFailsWith<Exception> {
                        restroomService.updateRestroom(nonExistentId, updateDto)
                    }

                assertTrue(exception.message?.contains("Failed to update restroom") == true)
                coVerify(exactly = 1) { restroomRepository.update(nonExistentId, updateDto) }
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
                    TestDataHelpers.createRestroomUpdateDto(
                        name = existingRestroom.name,
                        accessNote = existingRestroom.accessNote,
                        address = existingRestroom.address
                    )
                // Repository update will set updatedAt to current time, but createdAt should be preserved
                val updatedRestroom = existingRestroom.copy(updatedAt = Instant.now())
                coEvery { restroomRepository.update(existingRestroom.id, updateDto) } returns updatedRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

                assertEquals(updatedRestroom, actualResult)
                assertNotNull(actualResult)
                assertEquals(originalCreatedAt, actualResult!!.createdAt)
                coVerify(exactly = 1) { restroomRepository.update(existingRestroom.id, updateDto) }
            }

        @ParameterizedTest
        @EnumSource(RestroomStatus::class)
        @DisplayName("GIVEN existing restroom with different status WHEN updating THEN preserve status")
        fun given_existing_restroom_with_different_status_when_updating_then_preserve_status(status: RestroomStatus) =
            runTest {
                val existingRestroom = TestDataHelpers.createRestroomResponseDto(status = status)
                val updateDto = TestDataHelpers.createRestroomUpdateDto()
                // Repository update preserves status from existing restroom
                val updatedRestroom = existingRestroom.copy(updatedAt = Instant.now())
                coEvery { restroomRepository.update(existingRestroom.id, updateDto) } returns updatedRestroom

                val actualResult = restroomService.updateRestroom(existingRestroom.id, updateDto)

                assertEquals(updatedRestroom, actualResult)
                assertNotNull(actualResult)
                assertEquals(status, actualResult!!.status)
                coVerify(exactly = 1) { restroomRepository.update(existingRestroom.id, updateDto) }
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
                val pagination = PaginationRequest(page = 1, size = 3)
                val expectedPage =
                    PageResponse(
                        content = TestDataHelpers.createRestroomList(3),
                        page = 1,
                        size = 3,
                        totalElements = 10,
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
                val pagination = PaginationRequest(page = 0, size = 10)
                val expectedPage =
                    PageResponse(
                        content = emptyList<RestroomResponseDto>(),
                        page = 0,
                        size = 10,
                        totalElements = 0,
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
                val paginationRequest = PaginationRequest(page = 0, size = 5)
                val expectedPage =
                    PageResponse(
                        content = TestDataHelpers.createRestroomList(5),
                        page = 0,
                        size = 5,
                        totalElements = 7,
                        totalPages = 2,
                        first = true,
                        last = false
                    )
                coEvery { restroomRepository.findByCityId(cityId, paginationRequest) } returns expectedPage

                val actualPage = restroomService.getRestroomsByCity(cityId, paginationRequest)

                assertEquals(expectedPage.content, actualPage.content)
                assertEquals(expectedPage.page, actualPage.page)
                assertEquals(expectedPage.size, actualPage.size)
                coVerify(exactly = 1) { restroomRepository.findByCityId(cityId, paginationRequest) }
            }
    }
}
