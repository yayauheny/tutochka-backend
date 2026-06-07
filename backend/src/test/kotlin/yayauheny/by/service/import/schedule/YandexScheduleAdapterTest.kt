package yayauheny.by.service.import.schedule

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import yayauheny.by.model.schedule.Weekday

class YandexScheduleAdapterTest {
    private val adapter = YandexScheduleAdapter()
    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `toSchedule parses single interval day`() {
        val rawSchedule = objectMapper.readTree("""{"mon":{"from":"10:00","to":"18:00"}}""")

        val schedule = adapter.toSchedule(rawSchedule)

        assertTrue(schedule.hasDay(Weekday.MON))
        assertEquals(1, schedule.days[Weekday.MON]!!.workingHours.size)
        assertEquals("10:00", schedule.days[Weekday.MON]!!.workingHours.first().from.toString())
        assertEquals("18:00", schedule.days[Weekday.MON]!!.workingHours.first().to.toString())
    }

    @Test
    fun `toSchedule prefers structured intervals when present`() {
        val rawSchedule = objectMapper.readTree(
            """{"mon":{"from":"09:00","to":"21:00","intervals":[{"from":"09:00","to":"12:00"},{"from":"12:30","to":"21:00"}]}}"""
        )

        val schedule = adapter.toSchedule(rawSchedule)

        assertEquals(2, schedule.days[Weekday.MON]!!.workingHours.size)
        assertEquals("12:30", schedule.days[Weekday.MON]!!.workingHours[1].from.toString())
    }

    @Test
    fun `toSchedule ignores unsupported day keys`() {
        val rawSchedule = objectMapper.readTree("""{"holiday":{"from":"10:00","to":"18:00"}}""")

        val schedule = adapter.toSchedule(rawSchedule)

        assertTrue(schedule.isEmpty())
    }
}
