package yayauheny.by.model.schedule

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

/**
 * Utility functions for working with Schedule
 */
object ScheduleUtils {
    /**
     * Check if the restroom is currently open based on schedule
     * @param schedule schedule to check
     * @param zoneId timezone (defaults to system default)
     * @return true if open now, false otherwise
     */
    fun isOpenNow(
        schedule: Schedule,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean = isOpenAt(schedule, Instant.now(), zoneId)

    /**
     * Check if the restroom is open at a specific instant
     * @param schedule schedule to check
     * @param instant specific moment in time
     * @param zoneId timezone
     * @return true if open at that moment, false otherwise
     */
    fun isOpenAt(
        schedule: Schedule,
        instant: Instant,
        zoneId: ZoneId
    ): Boolean {
        if (schedule.isEmpty()) {
            return false // No schedule means closed
        }

        if (schedule.is24x7) {
            return true
        }

        val zonedDateTime = instant.atZone(zoneId)
        val currentDayOfWeek = zonedDateTime.dayOfWeek
        val currentTime = zonedDateTime.toLocalTime()

        // Convert DayOfWeek to Weekday
        val weekdayToday =
            when (currentDayOfWeek) {
                DayOfWeek.MONDAY -> Weekday.MON
                DayOfWeek.TUESDAY -> Weekday.TUE
                DayOfWeek.WEDNESDAY -> Weekday.WED
                DayOfWeek.THURSDAY -> Weekday.THU
                DayOfWeek.FRIDAY -> Weekday.FRI
                DayOfWeek.SATURDAY -> Weekday.SAT
                DayOfWeek.SUNDAY -> Weekday.SUN
            }

        // 1) Check normal intervals for today
        schedule.days[weekdayToday]?.let { daySchedule ->
            if (daySchedule.workingHours.any { interval ->
                    isTimeInInterval(currentTime, interval.from, interval.to)
                }
            ) {
                return true
            }
        }

        // 2) Check intervals that overflow from yesterday (midnight crossover)
        val weekdayYesterday = previous(weekdayToday)
        schedule.days[weekdayYesterday]?.let { daySchedule ->
            if (daySchedule.workingHours.any { interval ->
                    // Only check intervals that cross midnight
                    interval.from > interval.to && isTimeInInterval(currentTime, interval.from, interval.to)
                }
            ) {
                return true
            }
        }

        return false
    }

    /**
     * Get previous weekday
     */
    private fun previous(weekday: Weekday): Weekday {
        return when (weekday) {
            Weekday.MON -> Weekday.SUN
            Weekday.TUE -> Weekday.MON
            Weekday.WED -> Weekday.TUE
            Weekday.THU -> Weekday.WED
            Weekday.FRI -> Weekday.THU
            Weekday.SAT -> Weekday.FRI
            Weekday.SUN -> Weekday.SAT
        }
    }

    /**
     * Check if a time falls within an interval (handles midnight crossover)
     */
    private fun isTimeInInterval(
        time: LocalTime,
        from: LocalTime,
        to: LocalTime
    ): Boolean {
        return if (from <= to) {
            // Normal case: from 09:00 to 18:00
            time >= from && time <= to
        } else {
            // Midnight crossover: from 22:00 to 02:00
            time >= from || time <= to
        }
    }
}
