package yayauheny.by.unit.mapper

import io.mockk.every
import io.mockk.mockk
import org.jooq.Record
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import yayauheny.by.common.mapper.CountryMapper
import yayauheny.by.tables.references.COUNTRIES
import java.util.UUID

@DisplayName("CountryMapper Tests")
class CountryMapperTest {
    @Nested
    @DisplayName("mapFromRecord Tests")
    inner class MapFromRecordTests {
        @Test
        @DisplayName("GIVEN valid record WHEN mapFromRecord THEN return CountryResponseDto with all fields")
        fun mapFromRecord_returns_correct_dto() {
            val testId = UUID.randomUUID()
            val testCode = "US"
            val testNameRu = "США"
            val testNameEn = "United States"

            val mockRecord = mockk<Record>(relaxed = true)
            every { mockRecord[COUNTRIES.ID] } returns testId
            every { mockRecord[COUNTRIES.CODE] } returns testCode
            every { mockRecord[COUNTRIES.NAME_RU] } returns testNameRu
            every { mockRecord[COUNTRIES.NAME_EN] } returns testNameEn

            val result = CountryMapper.mapFromRecord(mockRecord)

            assertNotNull(result)
            assertEquals(testId, result.id)
            assertEquals(testCode, result.code)
            assertEquals(testNameRu, result.nameRu)
            assertEquals(testNameEn, result.nameEn)
        }
    }
}
