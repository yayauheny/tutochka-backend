package yayauheny.by.unit.mapper

import io.mockk.every
import io.mockk.mockk
import org.jooq.Record
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import yayauheny.by.common.mapper.CityMapper
import by.yayauheny.shared.dto.LatLon
import yayauheny.by.tables.references.CITIES
import java.util.UUID

@DisplayName("CityMapper Tests")
class CityMapperTest {
    @Nested
    @DisplayName("mapFromRecord Tests")
    inner class MapFromRecordTests {
        @Test
        @DisplayName("GIVEN valid record WHEN mapFromRecord THEN return CityResponseDto with all fields")
        fun mapFromRecord_returns_correct_dto() {
            val testId = UUID.randomUUID()
            val testCountryId = UUID.randomUUID()
            val testNameRu = "Москва"
            val testNameEn = "Moscow"
            val testRegion = "Московская область"
            val testLat = 55.7558
            val testLon = 37.6176

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.javaObjectType) } returns testLat
            every { mockRecord.get("lon", Double::class.javaObjectType) } returns testLon
            every { mockRecord[CITIES.ID] } returns testId
            every { mockRecord[CITIES.COUNTRY_ID] } returns testCountryId
            every { mockRecord[CITIES.NAME_RU] } returns testNameRu
            every { mockRecord[CITIES.NAME_EN] } returns testNameEn
            every { mockRecord[CITIES.REGION] } returns testRegion

            val result = CityMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertEquals(testId, result.id)
            assertEquals(testCountryId, result.countryId)
            assertEquals(testNameRu, result.nameRu)
            assertEquals(testNameEn, result.nameEn)
            assertEquals(testRegion, result.region)
            assertEquals(LatLon(lat = testLat, lon = testLon), result.coordinates)
        }

        @Test
        @DisplayName("GIVEN record with null region WHEN mapFromRecord THEN return DTO with null region")
        fun mapFromRecord_with_null_region_returns_dto_with_null_region() {
            val testId = UUID.randomUUID()
            val testCountryId = UUID.randomUUID()
            val testNameRu = "Москва"
            val testNameEn = "Moscow"
            val testLat = 55.7558
            val testLon = 37.6176

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.javaObjectType) } returns testLat
            every { mockRecord.get("lon", Double::class.javaObjectType) } returns testLon
            every { mockRecord[CITIES.ID] } returns testId
            every { mockRecord[CITIES.COUNTRY_ID] } returns testCountryId
            every { mockRecord[CITIES.NAME_RU] } returns testNameRu
            every { mockRecord[CITIES.NAME_EN] } returns testNameEn
            every { mockRecord[CITIES.REGION] } returns null

            val result = CityMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertNull(result.region)
        }

        @Test
        @DisplayName("GIVEN record with null lat WHEN mapFromRecord THEN throw IllegalArgumentException")
        fun mapFromRecord_with_null_lat_throws_exception() {
            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.javaObjectType) } returns null
            every { mockRecord.get("lon", Double::class.javaObjectType) } returns 37.6176

            assertThrows<IllegalArgumentException> {
                CityMapper.mapFromRecord(mockRecord)
            }
        }

        @Test
        @DisplayName("GIVEN record with null lon WHEN mapFromRecord THEN throw IllegalArgumentException")
        fun mapFromRecord_with_null_lon_throws_exception() {
            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord.get("lat", Double::class.javaObjectType) } returns 55.7558
            every { mockRecord.get("lon", Double::class.javaObjectType) } returns null

            assertThrows<IllegalArgumentException> {
                CityMapper.mapFromRecord(mockRecord)
            }
        }
    }
}
