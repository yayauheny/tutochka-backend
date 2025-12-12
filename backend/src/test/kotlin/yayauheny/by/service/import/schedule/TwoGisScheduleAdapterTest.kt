package yayauheny.by.service.import.schedule

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.model.schedule.Schedule
import yayauheny.by.model.schedule.Weekday
import java.time.LocalTime

class TwoGisScheduleAdapterTest {
    private val adapter = TwoGisScheduleAdapter()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `supports returns true for TWO_GIS provider`() {
        assertTrue(adapter.supports(ImportProvider.TWO_GIS))
    }

    @Test
    fun `supports returns false for other providers`() {
        assertFalse(adapter.supports(ImportProvider.YANDEX_MAPS))
        assertFalse(adapter.supports(ImportProvider.GOOGLE_MAPS))
    }

    @Test
    fun `toSchedule returns EMPTY for null input`() {
        val schedule = adapter.toSchedule(null)
        assertEquals(Schedule.EMPTY, schedule)
        assertTrue(schedule.isEmpty())
    }

    @Test
    fun `toSchedule returns EMPTY for empty JSON`() {
        val emptyJson = buildJsonObject { }
        val schedule = adapter.toSchedule(emptyJson)
        assertEquals(Schedule.EMPTY, schedule)
    }

    @Test
    fun `toSchedule parses 24x7 schedule correctly`() {
        val jsonSchedule =
            buildJsonObject {
                put("is_24x7", true)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        assertTrue(schedule.is24x7)
        assertTrue(schedule.days.isEmpty())
    }

    @Test
    fun `toSchedule parses single day schedule correctly`() {
        val jsonSchedule =
            buildJsonObject {
                put(
                    "Mon",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [{"from": "09:00", "to": "18:00"}]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put("is_24x7", false)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        assertFalse(schedule.is24x7)
        assertTrue(schedule.hasDay(Weekday.MON))

        val mondaySchedule = schedule.days[Weekday.MON]!!
        assertEquals(1, mondaySchedule.workingHours.size)
        assertEquals(LocalTime.of(9, 0), mondaySchedule.workingHours[0].from)
        assertEquals(LocalTime.of(18, 0), mondaySchedule.workingHours[0].to)
    }

    @Test
    fun `toSchedule parses multiple days schedule correctly`() {
        val jsonSchedule =
            buildJsonObject {
                put(
                    "Mon",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [{"from": "09:00", "to": "18:00"}]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put(
                    "Tue",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [{"from": "10:00", "to": "19:00"}]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put("is_24x7", false)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        assertTrue(schedule.hasDay(Weekday.MON))
        assertTrue(schedule.hasDay(Weekday.TUE))
        assertFalse(schedule.hasDay(Weekday.WED))

        assertEquals(LocalTime.of(9, 0), schedule.days[Weekday.MON]!!.workingHours[0].from)
        assertEquals(LocalTime.of(10, 0), schedule.days[Weekday.TUE]!!.workingHours[0].from)
    }

    @Test
    fun `toSchedule handles multiple intervals per day`() {
        val jsonSchedule =
            buildJsonObject {
                put(
                    "Mon",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [
                                        {"from": "09:00", "to": "12:00"},
                                        {"from": "13:00", "to": "18:00"}
                                    ]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put("is_24x7", false)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        val mondaySchedule = schedule.days[Weekday.MON]!!
        assertEquals(2, mondaySchedule.workingHours.size)
        assertEquals(LocalTime.of(9, 0), mondaySchedule.workingHours[0].from)
        assertEquals(LocalTime.of(12, 0), mondaySchedule.workingHours[0].to)
        assertEquals(LocalTime.of(13, 0), mondaySchedule.workingHours[1].from)
        assertEquals(LocalTime.of(18, 0), mondaySchedule.workingHours[1].to)
    }

    @Test
    fun `toSchedule handles midnight crossover intervals`() {
        val jsonSchedule =
            buildJsonObject {
                put(
                    "Mon",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [{"from": "22:00", "to": "02:00"}]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put("is_24x7", false)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        val mondaySchedule = schedule.days[Weekday.MON]!!
        assertEquals(1, mondaySchedule.workingHours.size)
        assertEquals(LocalTime.of(22, 0), mondaySchedule.workingHours[0].from)
        assertEquals(LocalTime.of(2, 0), mondaySchedule.workingHours[0].to)
    }

    @Test
    fun `toSchedule skips invalid time formats gracefully`() {
        val jsonSchedule =
            buildJsonObject {
                put(
                    "Mon",
                    buildJsonObject {
                        put(
                            "working_hours",
                            json
                                .parseToJsonElement(
                                    """
                                    [
                                        {"from": "09:00", "to": "18:00"},
                                        {"from": "invalid", "to": "time"}
                                    ]
                                    """.trimIndent()
                                ).jsonArray
                        )
                    }
                )
                put("is_24x7", false)
            }

        val schedule = adapter.toSchedule(jsonSchedule)
        val mondaySchedule = schedule.days[Weekday.MON]!!
        // Should only have one valid interval
        assertEquals(1, mondaySchedule.workingHours.size)
    }
}
