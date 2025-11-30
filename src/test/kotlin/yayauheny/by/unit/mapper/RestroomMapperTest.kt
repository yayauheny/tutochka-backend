package yayauheny.by.unit.mapper

import io.mockk.every
import io.mockk.mockk
import org.jooq.JSONB
import org.jooq.Record
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.common.mapper.RestroomMapper
import yayauheny.by.model.LatLon
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.tables.references.RESTROOMS
import java.time.Instant
import java.util.UUID

@DisplayName("RestroomMapper Tests")
class RestroomMapperTest {
    @Nested
    @DisplayName("mapFromRecord Tests")
    inner class MapFromRecordTests {
        @Test
        @DisplayName("GIVEN valid record WHEN mapFromRecord THEN return RestroomResponseDto with all fields")
        fun mapFromRecord_returns_correct_dto() {
            val testId = UUID.randomUUID()
            val testCityId = UUID.randomUUID()
            val testName = "Test Restroom"
            val testDescription = "Test Description"
            val testAddress = "Test Address"
            val testLat = 55.7558
            val testLon = 37.6176
            val testFeeType = FeeType.FREE
            val testAccessibilityType = AccessibilityType.DISABLED
            val testDataSource = DataSourceType.USER
            val testStatus = RestroomStatus.ACTIVE
            val testCreatedAt = Instant.now()
            val testUpdatedAt = Instant.now()

            val mockRecord = mockk<Record>(relaxed = true)
            // Mock reqDouble calls
            every { mockRecord.get("lat", Double::class.java) } returns testLat
            every { mockRecord.get("lon", Double::class.java) } returns testLon
            // Mock field access
            every { mockRecord[RESTROOMS.ID] } returns testId
            every { mockRecord[RESTROOMS.CITY_ID] } returns testCityId
            every { mockRecord[RESTROOMS.NAME] } returns testName
            every { mockRecord[RESTROOMS.DESCRIPTION] } returns testDescription
            every { mockRecord[RESTROOMS.ADDRESS] } returns testAddress
            every { mockRecord[RESTROOMS.PHONES] } returns null
            every { mockRecord[RESTROOMS.WORK_TIME] } returns null
            every { mockRecord[RESTROOMS.FEE_TYPE] } returns testFeeType.name
            every { mockRecord[RESTROOMS.ACCESSIBILITY_TYPE] } returns testAccessibilityType.name
            every { mockRecord[RESTROOMS.DATA_SOURCE] } returns testDataSource.name
            every { mockRecord[RESTROOMS.STATUS] } returns testStatus.name
            every { mockRecord[RESTROOMS.AMENITIES] } returns null
            every { mockRecord[RESTROOMS.PARENT_PLACE_NAME] } returns null
            every { mockRecord[RESTROOMS.PARENT_PLACE_TYPE] } returns null
            every { mockRecord[RESTROOMS.INHERIT_PARENT_SCHEDULE] } returns null
            every { mockRecord[RESTROOMS.CREATED_AT] } returns testCreatedAt
            every { mockRecord[RESTROOMS.UPDATED_AT] } returns testUpdatedAt

            val result = RestroomMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertEquals(testId, result.id)
            assertEquals(testCityId, result.cityId)
            assertEquals(testName, result.name)
            assertEquals(testDescription, result.description)
            assertEquals(testAddress, result.address)
            assertEquals(LatLon(lat = testLat, lon = testLon), result.coordinates)
            assertEquals(testFeeType, result.feeType)
            assertEquals(testAccessibilityType, result.accessibilityType)
            assertEquals(testDataSource, result.dataSource)
            assertEquals(testStatus, result.status)
            assertEquals(testCreatedAt, result.createdAt)
            assertEquals(testUpdatedAt, result.updatedAt)
            assertNull(result.parentPlaceName)
            assertNull(result.parentPlaceType)
            assertEquals(false, result.inheritParentSchedule)
        }

        @Test
        @DisplayName("GIVEN record with null optional fields WHEN mapFromRecord THEN return DTO with null values")
        fun mapFromRecord_with_null_optional_fields_returns_dto_with_nulls() {
            val testId = UUID.randomUUID()
            val testAddress = "Test Address"
            val testLat = 55.7558
            val testLon = 37.6176
            val testFeeType = FeeType.FREE
            val testAccessibilityType = AccessibilityType.DISABLED
            val testDataSource = DataSourceType.USER
            val testStatus = RestroomStatus.ACTIVE
            val testCreatedAt = Instant.now()
            val testUpdatedAt = Instant.now()

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.java) } returns testLat
            every { mockRecord.get("lon", Double::class.java) } returns testLon
            every { mockRecord[RESTROOMS.ID] } returns testId
            every { mockRecord[RESTROOMS.CITY_ID] } returns null
            every { mockRecord[RESTROOMS.NAME] } returns null
            every { mockRecord[RESTROOMS.DESCRIPTION] } returns null
            every { mockRecord[RESTROOMS.ADDRESS] } returns testAddress
            every { mockRecord[RESTROOMS.PHONES] } returns null
            every { mockRecord[RESTROOMS.WORK_TIME] } returns null
            every { mockRecord[RESTROOMS.FEE_TYPE] } returns testFeeType.name
            every { mockRecord[RESTROOMS.ACCESSIBILITY_TYPE] } returns testAccessibilityType.name
            every { mockRecord[RESTROOMS.DATA_SOURCE] } returns testDataSource.name
            every { mockRecord[RESTROOMS.STATUS] } returns testStatus.name
            every { mockRecord[RESTROOMS.AMENITIES] } returns null
            every { mockRecord[RESTROOMS.PARENT_PLACE_NAME] } returns null
            every { mockRecord[RESTROOMS.PARENT_PLACE_TYPE] } returns null
            every { mockRecord[RESTROOMS.INHERIT_PARENT_SCHEDULE] } returns null
            every { mockRecord[RESTROOMS.CREATED_AT] } returns testCreatedAt
            every { mockRecord[RESTROOMS.UPDATED_AT] } returns testUpdatedAt

            val result = RestroomMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertNull(result.cityId)
            assertNull(result.name)
            assertNull(result.description)
        }

        @Test
        @DisplayName("GIVEN record with JSONB fields WHEN mapFromRecord THEN return DTO with parsed JSON")
        fun mapFromRecord_with_jsonb_fields_returns_parsed_json() {
            val testId = UUID.randomUUID()
            val testAddress = "Test Address"
            val testLat = 55.7558
            val testLon = 37.6176
            val testFeeType = FeeType.FREE
            val testAccessibilityType = AccessibilityType.DISABLED
            val testDataSource = DataSourceType.USER
            val testStatus = RestroomStatus.ACTIVE
            val testCreatedAt = Instant.now()
            val testUpdatedAt = Instant.now()
            val testPhonesJson = JSONB.jsonb("""{"mobile":"+1234567890"}""")
            val testWorkTimeJson = JSONB.jsonb("""{"monday":"09:00-18:00"}""")
            val testAmenitiesJson = JSONB.jsonb("""{"wifi":true}""")

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.java) } returns testLat
            every { mockRecord.get("lon", Double::class.java) } returns testLon
            every { mockRecord[RESTROOMS.ID] } returns testId
            every { mockRecord[RESTROOMS.CITY_ID] } returns null
            every { mockRecord[RESTROOMS.NAME] } returns null
            every { mockRecord[RESTROOMS.DESCRIPTION] } returns null
            every { mockRecord[RESTROOMS.ADDRESS] } returns testAddress
            every { mockRecord[RESTROOMS.PHONES] } returns testPhonesJson
            every { mockRecord[RESTROOMS.WORK_TIME] } returns testWorkTimeJson
            every { mockRecord[RESTROOMS.FEE_TYPE] } returns testFeeType.name
            every { mockRecord[RESTROOMS.ACCESSIBILITY_TYPE] } returns testAccessibilityType.name
            every { mockRecord[RESTROOMS.DATA_SOURCE] } returns testDataSource.name
            every { mockRecord[RESTROOMS.STATUS] } returns testStatus.name
            every { mockRecord[RESTROOMS.AMENITIES] } returns testAmenitiesJson
            every { mockRecord[RESTROOMS.PARENT_PLACE_NAME] } returns null
            every { mockRecord[RESTROOMS.PARENT_PLACE_TYPE] } returns null
            every { mockRecord[RESTROOMS.INHERIT_PARENT_SCHEDULE] } returns true
            every { mockRecord[RESTROOMS.CREATED_AT] } returns testCreatedAt
            every { mockRecord[RESTROOMS.UPDATED_AT] } returns testUpdatedAt

            val result = RestroomMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertNotNull(result.phones)
            assertNotNull(result.workTime)
            assertNotNull(result.amenities)
            assertEquals(true, result.inheritParentSchedule)
        }
    }

    @Nested
    @DisplayName("mapToNearestRestroom Tests")
    inner class MapToNearestRestroomTests {
        @Test
        @DisplayName("GIVEN valid record WHEN mapToNearestRestroom THEN return NearestRestroomResponseDto")
        fun mapToNearestRestroom_returns_correct_dto() {
            val testId = UUID.randomUUID()
            val testName = "Test Restroom"
            val testAddress = "Test Address"
            val testLat = 55.7558
            val testLon = 37.6176
            val testDistance = 150.5
            val testFeeType = FeeType.FREE

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.java) } returns testLat
            every { mockRecord.get("lon", Double::class.java) } returns testLon
            every { mockRecord[RESTROOMS.ID] } returns testId
            every { mockRecord[RESTROOMS.NAME] } returns testName
            every { mockRecord[RESTROOMS.ADDRESS] } returns testAddress
            every { mockRecord[RESTROOMS.FEE_TYPE] } returns testFeeType.name

            val result = RestroomMapper.mapToNearestRestroom(mockRecord, testDistance)

            assertNotNull(result)
            assertEquals(testId, result.id)
            assertEquals(testName, result.name)
            assertEquals(testAddress, result.address)
            assertEquals(LatLon(lat = testLat, lon = testLon), result.coordinates)
            assertEquals(testDistance, result.distanceMeters)
            assertEquals(testFeeType, result.feeType)
            assertNull(result.isOpen)
        }
    }
}
