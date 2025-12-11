package yayauheny.by.service.import.schedule

import yayauheny.by.model.import.ImportProvider
import yayauheny.by.model.schedule.DaySchedule
import yayauheny.by.model.schedule.Schedule
import yayauheny.by.model.schedule.Weekday
import yayauheny.by.model.schedule.WorkingInterval
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import java.time.LocalTime
import java.util.EnumMap

/**
 * Adapter for converting 2ГИС schedule format to our canonical Schedule format
 *
 * 2ГИС format example:
 * {
 *   "Mon": { "working_hours": [{ "from": "00:00", "to": "24:00" }] },
 *   "Tue": { "working_hours": [{ "from": "00:00", "to": "24:00" }] },
 *   ...
 *   "is_24x7": true
 * }
 */
class TwoGisScheduleAdapter : ScheduleAdapter {
    override fun supports(provider: ImportProvider): Boolean {
        return provider == ImportProvider.TWO_GIS
    }

    override fun toSchedule(rawSchedule: JsonObject?): Schedule {
        if (rawSchedule == null || rawSchedule.isEmpty()) {
            return Schedule.EMPTY
        }

        val is24x7 =
            try {
                rawSchedule["is_24x7"]?.let { element ->
                    if (element is JsonPrimitive) {
                        element.content == "true" || element.content == "1"
                    } else {
                        false
                    }
                } ?: false
            } catch (e: Exception) {
                false
            }

        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)

        // Parse each day of the week
        val dayKeys = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        for (dayKey in dayKeys) {
            val dayElement = rawSchedule[dayKey]
            if (dayElement != null && dayElement is JsonObject) {
                val workingHours = parseWorkingHours(dayElement)
                if (workingHours.isNotEmpty()) {
                    val weekday = Weekday.fromTwoGisKey(dayKey)
                    days[weekday] = DaySchedule(workingHours)
                }
            }
        }

        return Schedule(days, is24x7)
    }

    private fun parseWorkingHours(dayNode: JsonObject): List<WorkingInterval> {
        val workingHoursElement = dayNode["working_hours"]
        if (workingHoursElement == null || workingHoursElement !is JsonArray) {
            return emptyList()
        }

        return workingHoursElement.mapNotNull { intervalElement ->
            if (intervalElement !is JsonObject) {
                return@mapNotNull null
            }

            val fromElement = intervalElement["from"]
            val toElement = intervalElement["to"]

            val fromStr = (fromElement as? JsonPrimitive)?.content
            val toStr = (toElement as? JsonPrimitive)?.content

            if (fromStr != null && toStr != null) {
                try {
                    val from = LocalTime.parse(fromStr)
                    val to = LocalTime.parse(toStr)
                    WorkingInterval(from, to)
                } catch (e: Exception) {
                    null // Skip invalid intervals
                }
            } else {
                null
            }
        }
    }
}
