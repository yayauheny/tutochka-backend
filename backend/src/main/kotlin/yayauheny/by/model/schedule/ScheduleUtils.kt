package yayauheny.by.model.schedule

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Utility functions for working with Schedule
 */
object ScheduleUtils {
    /**
     * Check if the restroom is currently open based on schedule
     * @param schedule schedule to check
     * @param zoneId timezone (defaults to UTC)
     * @return true if open now, false otherwise
     */
    fun isOpenNow(
        schedule: Schedule,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean {
        if (schedule.isEmpty()) {
            return false // No schedule means closed
        }

        if (schedule.is24x7) {
            return true
        }

        val now = ZonedDateTime.now(zoneId)
        val currentDayOfWeek = now.dayOfWeek
        val currentTime = now.toLocalTime()

        // Convert DayOfWeek to Weekday
        val weekday =
            when (currentDayOfWeek) {
                DayOfWeek.MONDAY -> Weekday.MON
                DayOfWeek.TUESDAY -> Weekday.TUE
                DayOfWeek.WEDNESDAY -> Weekday.WED
                DayOfWeek.THURSDAY -> Weekday.THU
                DayOfWeek.FRIDAY -> Weekday.FRI
                DayOfWeek.SATURDAY -> Weekday.SAT
                DayOfWeek.SUNDAY -> Weekday.SUN
            }

        val daySchedule = schedule.days[weekday] ?: return false

        // Check if current time falls within any working interval
        return daySchedule.workingHours.any { interval ->
            isTimeInInterval(currentTime, interval.from, interval.to)
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
