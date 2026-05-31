package yayauheny.by.bot.util

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull

object WorkTimeFormatter {

    private val DAY_NAMES = mapOf(
        "Mon" to "Пн",
        "Tue" to "Вт",
        "Wed" to "Ср",
        "Thu" to "Чт",
        "Fri" to "Пт",
        "Sat" to "Сб",
        "Sun" to "Вс"
    )

    private val DAY_ORDER = arrayOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    fun formatWorkTime(workTime: JsonObject?): String {
        if (workTime == null || workTime.isEmpty()) {
            return "Время работы не указано"
        }

        if (isTrue(workTime["is_24x7"])) {
            return "Круглосуточно"
        }

        val result = java.lang.StringBuilder()
        val comment = extractComment(workTime)
        if (!comment.isNullOrBlank()) {
            result.append(comment).append("\n")
        }

        var firstDay = true

        for (day in DAY_ORDER) {
            if (workTime.containsKey(day)) {
                if (!firstDay) {
                    result.append("\n")
                }

                val dayName = DAY_NAMES[day]
                val daySchedule = formatDaySchedule(workTime[day])
                result.append(dayName).append(": ").append(daySchedule)
                firstDay = false
            }
        }

        if (firstDay && result.isEmpty()) {
            return "Время работы не указано"
        }

        return result.toString()
    }

    fun getSimplifiedWorkTime(workTime: JsonObject?): String? {
        if (workTime == null || workTime.isEmpty()) return null

        var firstDaySchedule: String? = null
        var allDaysSame = true

        for (day in DAY_ORDER) {
            if (workTime.containsKey(day)) {
                val daySchedule = formatDaySchedule(workTime[day])
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
            if (workTime.containsKey(day)) {
                val dayName = DAY_NAMES[day]
                val daySchedule = formatDaySchedule(workTime[day])
                return "$dayName: $daySchedule"
            }
        }

        return null
    }

    private fun extractComment(workTime: JsonObject): String? {
        return asString(workTime["comment"])
    }

    private fun formatDaySchedule(dayData: JsonElement?): String {
        if (dayData !is JsonObject) {
            return "не указано"
        }

        val workingHoursObj = dayData["working_hours"]
        if (workingHoursObj is JsonArray && workingHoursObj.isNotEmpty()) {
            val dayResult = StringBuilder()
            var firstPeriod = true
            for (period in workingHoursObj) {
                if (period is JsonObject) {
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

    private fun asString(value: JsonElement?): String? {
        return if (value is JsonPrimitive) value.content else value?.toString()
    }

    private fun isTrue(value: JsonElement?): Boolean {
        if (value !is JsonPrimitive) return false
        return value.booleanOrNull == true
    }

    private fun is24x7(from: String, to: String): Boolean {
        return (from == "00:00" && to == "24:00") || (from == "00:00" && to == "00:00")
    }
}
