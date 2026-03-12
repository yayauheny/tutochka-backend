package yayauheny.by.model.schedule

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.EnumMap
import yayauheny.by.util.ScheduleUtils

class ScheduleUtilsTest {
    private val minskZone = ZoneId.of("Europe/Minsk")

    @Test
    fun `isOpenNow returns false for empty schedule`() {
        val schedule = Schedule.EMPTY
        assertFalse(ScheduleUtils.isOpenNow(schedule))
    }

    @Test
    fun `isOpenNow returns true for 24x7 schedule`() {
        val schedule = Schedule(emptyMap(), is24x7 = true)
        assertTrue(ScheduleUtils.isOpenNow(schedule))
    }

    @Test
    fun `isOpenAt returns true when time is within working hours`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(WorkingInterval(LocalTime.of(9, 0), LocalTime.of(18, 0)))
            )

        val schedule = Schedule(days, false)
        val instant =
            LocalDateTime
                .of(2024, 1, 1, 10, 0) // Monday 10:00
                .atZone(minskZone)
                .toInstant()

        assertTrue(ScheduleUtils.isOpenAt(schedule, instant, minskZone))
    }

    @Test
    fun `isOpenAt returns false when time is outside working hours`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(WorkingInterval(LocalTime.of(9, 0), LocalTime.of(18, 0)))
            )

        val schedule = Schedule(days, false)
        val instant =
            LocalDateTime
                .of(2024, 1, 1, 20, 0) // Monday 20:00 (closed)
                .atZone(minskZone)
                .toInstant()

        assertFalse(ScheduleUtils.isOpenAt(schedule, instant, minskZone))
    }

    @Test
    fun `isOpenAt handles midnight crossover interval on same day`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(WorkingInterval(LocalTime.of(22, 0), LocalTime.of(2, 0))) // 10 PM to 2 AM
            )

        val schedule = Schedule(days, false)

        // Monday 23:00 - should be open
        val instantMonday =
            LocalDateTime
                .of(2024, 1, 1, 23, 0) // Monday 23:00
                .atZone(minskZone)
                .toInstant()
        assertTrue(ScheduleUtils.isOpenAt(schedule, instantMonday, minskZone))

        // Monday 01:00 - should be open (still within Monday's interval)
        val instantMondayEarly =
            LocalDateTime
                .of(2024, 1, 1, 1, 0) // Monday 01:00
                .atZone(minskZone)
                .toInstant()
        assertTrue(ScheduleUtils.isOpenAt(schedule, instantMondayEarly, minskZone))
    }

    @Test
    fun `isOpenAt handles midnight crossover interval on next day`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(WorkingInterval(LocalTime.of(22, 0), LocalTime.of(2, 0))) // 10 PM to 2 AM
            )

        val schedule = Schedule(days, false)

        // Tuesday 01:00 - should be open (overflow from Monday)
        val instantTuesday =
            LocalDateTime
                .of(2024, 1, 2, 1, 0) // Tuesday 01:00
                .atZone(minskZone)
                .toInstant()
        assertTrue(ScheduleUtils.isOpenAt(schedule, instantTuesday, minskZone))

        // Tuesday 03:00 - should be closed (outside interval)
        val instantTuesdayLate =
            LocalDateTime
                .of(2024, 1, 2, 3, 0) // Tuesday 03:00
                .atZone(minskZone)
                .toInstant()
        assertFalse(ScheduleUtils.isOpenAt(schedule, instantTuesdayLate, minskZone))
    }

    @Test
    fun `isOpenAt returns false for day not in schedule`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(WorkingInterval(LocalTime.of(9, 0), LocalTime.of(18, 0)))
            )

        val schedule = Schedule(days, false)
        val instant =
            LocalDateTime
                .of(2024, 1, 2, 10, 0) // Tuesday 10:00 (not in schedule)
                .atZone(minskZone)
                .toInstant()

        assertFalse(ScheduleUtils.isOpenAt(schedule, instant, minskZone))
    }

    @Test
    fun `isOpenAt handles multiple intervals per day`() {
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        days[Weekday.MON] =
            DaySchedule(
                listOf(
                    WorkingInterval(LocalTime.of(9, 0), LocalTime.of(12, 0)),
                    WorkingInterval(LocalTime.of(13, 0), LocalTime.of(18, 0))
                )
            )

        val schedule = Schedule(days, false)

        // Monday 10:00 - first interval
        val instant1 =
            LocalDateTime
                .of(2024, 1, 1, 10, 0)
                .atZone(minskZone)
                .toInstant()
        assertTrue(ScheduleUtils.isOpenAt(schedule, instant1, minskZone))

        // Monday 12:30 - break between intervals
        val instant2 =
            LocalDateTime
                .of(2024, 1, 1, 12, 30)
                .atZone(minskZone)
                .toInstant()
        assertFalse(ScheduleUtils.isOpenAt(schedule, instant2, minskZone))

        // Monday 15:00 - second interval
        val instant3 =
            LocalDateTime
                .of(2024, 1, 1, 15, 0)
                .atZone(minskZone)
                .toInstant()
        assertTrue(ScheduleUtils.isOpenAt(schedule, instant3, minskZone))
    }
}
