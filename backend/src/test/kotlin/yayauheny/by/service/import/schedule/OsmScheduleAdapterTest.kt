package yayauheny.by.service.import.schedule

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import yayauheny.by.model.schedule.Weekday

class OsmScheduleAdapterTest {
    private val adapter = OsmScheduleAdapter()
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `toSchedule parses simple daily range`() {
        val rawSchedule = objectMapper.readTree("""{"opening_hours":"07:00-23:00"}""")

        val schedule = adapter.toSchedule(rawSchedule)

        assertTrue(schedule.hasDay(Weekday.MON))
        assertEquals("07:00", schedule.days[Weekday.MON]!!.workingHours.first().from.toString())
        assertEquals("23:00", schedule.days[Weekday.MON]!!.workingHours.first().to.toString())
        assertTrue(schedule.hasDay(Weekday.SUN))
    }

    @Test
    fun `toSchedule returns empty for malformed string`() {
        val rawSchedule = objectMapper.readTree("""{"opening_hours":"not-a-time"}""")

        val schedule = adapter.toSchedule(rawSchedule)

        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `toSchedule returns empty for unsupported advanced expression`() {
        val rawSchedule = objectMapper.readTree("""{"opening_hours":"Mo-Fr 09:00-18:00"}""")

        val schedule = adapter.toSchedule(rawSchedule)

        assertTrue(schedule.isEmpty())
    }
}
