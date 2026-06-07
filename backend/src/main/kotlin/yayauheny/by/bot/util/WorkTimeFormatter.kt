package yayauheny.by.bot.util

import java.time.LocalTime
import yayauheny.by.model.schedule.Schedule
import yayauheny.by.model.schedule.Weekday

object WorkTimeFormatter {

    private val DAY_NAMES = mapOf(
        "Mon" to "Пн",
        "mon" to "Пн",
        "Tue" to "Вт",
        "tue" to "Вт",
        "Wed" to "Ср",
        "wed" to "Ср",
        "Thu" to "Чт",
        "thu" to "Чт",
        "Fri" to "Пт",
        "fri" to "Пт",
        "Sat" to "Сб",
        "sat" to "Сб",
        "Sun" to "Вс",
        "sun" to "Вс"
    )

    private val DAY_ORDER = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val WEEKDAY_ORDER = listOf(Weekday.MON, Weekday.TUE, Weekday.WED, Weekday.THU, Weekday.FRI, Weekday.SAT, Weekday.SUN)

    fun formatWorkTime(workTime: Map<String, Any>?): String {
        if (workTime.isNullOrEmpty()) {
            return "Время работы не указано"
        }

        val openingHours = asString(workTime["opening_hours"])
        if (!openingHours.isNullOrBlank()) {
            return openingHours
        }

        if (isTrue(workTime["is_24x7"])) {
            return "Круглосуточно"
        }

        val result = java.lang.StringBuilder()
        val comment = asString(workTime["comment"])
        if (!comment.isNullOrBlank()) {
            result.append(comment).append("\n")
        }

        var firstDay = true

        for (day in DAY_ORDER) {
            val dayData = findDayData(workTime, day)
            if (dayData != null) {
                if (!firstDay) {
                    result.append("\n")
                }

                val dayName = DAY_NAMES[day]
                val daySchedule = formatDaySchedule(dayData)
                result.append(dayName).append(": ").append(daySchedule)
                firstDay = false
            }
        }

        if (firstDay && result.isEmpty()) {
            return "Время работы не указано"
        }

        return result.toString()
    }

    fun formatSchedule(schedule: Schedule): String {
        if (schedule.isEmpty()) {
            return "Время работы не указано"
        }

        if (schedule.is24x7) {
            return "Круглосуточно"
        }

        val byDay = WEEKDAY_ORDER.associateWith { weekday ->
            schedule.days[weekday]?.workingHours
                ?.joinToString(", ") { interval -> formatInterval(interval.from, interval.to) }
        }

        val distinctNonNull = byDay.values.filterNotNull().distinct()
        if (distinctNonNull.size == 1 && byDay.values.none { it == null }) {
            return "Ежедневно: ${distinctNonNull.single()}"
        }

        return WEEKDAY_ORDER.mapNotNull { weekday ->
            val value = byDay[weekday] ?: return@mapNotNull null
            "${weekday.toRuLabel()}: $value"
        }.joinToString("\n")
    }

    fun getSimplifiedWorkTime(workTime: Map<String, Any>?): String? {
        if (workTime.isNullOrEmpty()) return null

        var firstDaySchedule: String? = null
        var allDaysSame = true

        for (day in DAY_ORDER) {
            val dayData = findDayData(workTime, day)
            if (dayData != null) {
                val daySchedule = formatDaySchedule(dayData)
                if (firstDaySchedule == null) {
                    firstDaySchedule = daySchedule
                } else if (firstDaySchedule != daySchedule) {
                    allDaysSame = false
                    break
                }
            }
        }

        if (allDaysSame && firstDaySchedule != null) {
            return "Ежедневно: $firstDaySchedule"
        }

        for (day in DAY_ORDER) {
            val dayData = findDayData(workTime, day)
            if (dayData != null) {
                val dayName = DAY_NAMES[day]
                val daySchedule = formatDaySchedule(dayData)
                return "$dayName: $daySchedule"
            }
        }

        return null
    }

    private fun formatDaySchedule(dayData: Any?): String {
        if (dayData !is Map<*, *>) {
            return "не указано"
        }

        val workingHoursObj = dayData["working_hours"] ?: dayData["intervals"]
        if (workingHoursObj is List<*> && workingHoursObj.isNotEmpty()) {
            val dayResult = StringBuilder()
            var firstPeriod = true
            for (period in workingHoursObj) {
                if (period is Map<*, *>) {
                    val from = asString(period["from"])
                    val to = asString(period["to"])
                    if (from.isNullOrBlank() || to.isNullOrBlank()) continue
                    if (!firstPeriod) {
                        dayResult.append(", ")
                    }
                    dayResult.append(if (is24x7(from, to)) "00:00-24:00" else "$from-$to")
                    firstPeriod = false
                }
            }
            if (dayResult.isNotEmpty()) {
                return dayResult.toString()
            }
        }

        if (dayData.containsKey("from") && dayData.containsKey("to")) {
            val from = asString(dayData["from"])
            val to = asString(dayData["to"])
            if (!from.isNullOrBlank() && !to.isNullOrBlank()) {
                return if (is24x7(from, to)) "00:00-24:00" else "$from-$to"
            }
        }
        return "не указано"
    }

    private fun asString(value: Any?): String? {
        return value?.toString()
    }

    private fun findDayData(
        workTime: Map<String, Any>,
        canonicalDay: String
    ): Any? {
        return workTime[canonicalDay] ?: workTime[canonicalDay.lowercase()]
    }

    private fun isTrue(value: Any?): Boolean {
        return value == true || value == "true"
    }

    private fun is24x7(from: String, to: String): Boolean {
        return (from == "00:00" && to == "24:00") || (from == "00:00" && to == "00:00")
    }

    private fun formatInterval(
        from: LocalTime,
        to: LocalTime
    ): String {
        val fromText = from.toString().padEnd(5, '0')
        val toText = if (to == LocalTime.MIDNIGHT) "24:00" else to.toString().padEnd(5, '0')
        return if (fromText == "00:00" && toText == "24:00") "00:00-24:00" else "$fromText-$toText"
    }

    private fun Weekday.toRuLabel(): String {
        return when (this) {
            Weekday.MON -> "Пн"
            Weekday.TUE -> "Вт"
            Weekday.WED -> "Ср"
            Weekday.THU -> "Чт"
            Weekday.FRI -> "Пт"
            Weekday.SAT -> "Сб"
            Weekday.SUN -> "Вс"
        }
    }
}
