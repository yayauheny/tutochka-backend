package yayauheny.by.model.schedule

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId

object ScheduleUtils {
    fun isOpenNow(
        schedule: Schedule,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Boolean = isOpenAt(schedule, Instant.now(), zoneId)

    fun isOpenAt(
        schedule: Schedule,
        instant: Instant,
        zoneId: ZoneId
    ): Boolean {
        if (schedule.isEmpty()) {
            return false
        }

        if (schedule.is24x7) {
            return true
        }

        val zonedDateTime = instant.atZone(zoneId)
        val currentDayOfWeek = zonedDateTime.dayOfWeek
        val currentTime = zonedDateTime.toLocalTime()

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

        schedule.days[weekdayToday]?.let { daySchedule ->
            if (daySchedule.workingHours.any { interval ->
                    isTimeInInterval(currentTime, interval.from, interval.to)
                }
            ) {
                return true
            }
        }

        val weekdayYesterday = previous(weekdayToday)
        schedule.days[weekdayYesterday]?.let { daySchedule ->
            if (daySchedule.workingHours.any { interval ->
                    interval.from > interval.to && isTimeInInterval(currentTime, interval.from, interval.to)
                }
            ) {
                return true
            }
        }

        return false
    }

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

    private fun isTimeInInterval(
        time: LocalTime,
        from: LocalTime,
        to: LocalTime
    ): Boolean {
        return if (from <= to) {
            time >= from && time <= to
        } else {
            time >= from || time <= to
        }
    }
}
