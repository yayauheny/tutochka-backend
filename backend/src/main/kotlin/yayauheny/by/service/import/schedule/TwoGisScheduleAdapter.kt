package yayauheny.by.service.import.schedule

import com.fasterxml.jackson.databind.JsonNode
import java.time.LocalTime
import java.util.EnumMap
import org.springframework.stereotype.Component
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.schedule.DaySchedule
import yayauheny.by.model.schedule.Schedule
import yayauheny.by.model.schedule.Weekday
import yayauheny.by.model.schedule.WorkingInterval

/**
 * Adapter for converting 2ГИС schedule format to our canonical Schedule format
 */
@Component
class TwoGisScheduleAdapter : ScheduleAdapter {
    override fun supports(provider: ImportProvider): Boolean {
        return provider == ImportProvider.TWO_GIS
    }

    override fun toSchedule(rawSchedule: JsonNode?): Schedule {
        if (rawSchedule == null || rawSchedule.isEmpty || !rawSchedule.isObject) {
            return Schedule.EMPTY
        }

        val is24x7 = if (rawSchedule.has("is_24x7")) {
            val node = rawSchedule.get("is_24x7")
            if (node.isBoolean) node.asBoolean() else node.asText() == "true" || node.asText() == "1"
        } else false

        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)

        val dayKeys = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for (dayKey in dayKeys) {
            if (rawSchedule.has(dayKey)) {
                val dayElement = rawSchedule.get(dayKey)
                if (dayElement.isObject) {
                    val workingHours = parseWorkingHours(dayElement)
                    if (workingHours.isNotEmpty()) {
                        val weekday = Weekday.fromTwoGisKey(dayKey)
                        days[weekday] = DaySchedule(workingHours)
                    }
                }
            }
        }

        return Schedule(days, is24x7)
    }

    private fun parseWorkingHours(dayNode: JsonNode): List<WorkingInterval> {
        if (!dayNode.has("working_hours")) return emptyList()
        val workingHoursElement = dayNode.get("working_hours")
        if (!workingHoursElement.isArray) {
            return emptyList()
        }

        val result = mutableListOf<WorkingInterval>()
        for (intervalElement in workingHoursElement) {
            if (!intervalElement.isObject) continue

            val fromNode = intervalElement.get("from")
            val toNode = intervalElement.get("to")

            val fromStr = if (fromNode?.isTextual == true) fromNode.asText() else null
            val toStr = if (toNode?.isTextual == true) toNode.asText() else null

            if (fromStr != null && toStr != null) {
                val from = parseTimeOrNull(fromStr)
                val to = parseTimeOrNull(toStr)
                if (from != null && to != null) {
                    result.add(WorkingInterval(from, to))
                }
            }
        }
        return result
    }

    private fun parseTimeOrNull(value: String): LocalTime? {
        return if (value == "24:00") {
            LocalTime.MIDNIGHT
        } else {
            try {
                LocalTime.parse(value)
            } catch (e: Exception) {
                null
            }
        }
    }
}
