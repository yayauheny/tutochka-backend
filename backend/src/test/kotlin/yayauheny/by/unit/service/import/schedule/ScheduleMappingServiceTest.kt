package yayauheny.by.unit.service.import.schedule

import com.fasterxml.jackson.databind.JsonNode
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.schedule.Schedule
import yayauheny.by.service.import.schedule.ScheduleAdapter
import yayauheny.by.service.import.schedule.ScheduleMappingService

class ScheduleMappingServiceTest {
    @Test
    fun `mapSchedule selects adapter that supports provider`() {
        val adapter = mockk<ScheduleAdapter>()
        val rawSchedule = mockk<JsonNode>()
        val expected = Schedule.EMPTY
        every { adapter.supports(ImportProvider.TWO_GIS) } returns true
        every { adapter.toSchedule(rawSchedule) } returns expected
        val service = ScheduleMappingService(listOf(adapter))

        val result = service.mapSchedule(ImportProvider.TWO_GIS, rawSchedule)

        assertEquals(expected, result)
        verify { adapter.toSchedule(rawSchedule) }
    }

    @Test
    fun `mapSchedule ignores non matching adapters and delegates unchanged raw schedule`() {
        val ignored = mockk<ScheduleAdapter>()
        val chosen = mockk<ScheduleAdapter>()
        val rawSchedule = mockk<JsonNode>()
        val expected = Schedule.EMPTY
        every { ignored.supports(ImportProvider.YANDEX_MAPS) } returns false
        every { chosen.supports(ImportProvider.YANDEX_MAPS) } returns true
        every { chosen.toSchedule(rawSchedule) } returns expected
        val service = ScheduleMappingService(listOf(ignored, chosen))

        val result = service.mapSchedule(ImportProvider.YANDEX_MAPS, rawSchedule)

        assertEquals(expected, result)
        verify { chosen.toSchedule(rawSchedule) }
    }

    @Test
    fun `mapSchedule throws when no adapter supports provider`() {
        val adapter = mockk<ScheduleAdapter>()
        every { adapter.supports(ImportProvider.OSM) } returns false
        val service = ScheduleMappingService(listOf(adapter))

        val error =
            assertFailsWith<IllegalArgumentException> {
                service.mapSchedule(ImportProvider.OSM, null)
            }

        assertEquals("No schedule adapter found for provider: OSM", error.message)
    }
}
