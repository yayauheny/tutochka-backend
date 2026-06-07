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

@Component
class OsmScheduleAdapter : ScheduleAdapter {
    private val simpleDailyRangeRegex = Regex("""^(\d{2}:\d{2})-(\d{2}:\d{2})$""")

    override fun supports(provider: ImportProvider): Boolean {
        return provider == ImportProvider.OSM
    }

    override fun toSchedule(rawSchedule: JsonNode?): Schedule {
        if (rawSchedule == null || rawSchedule.isEmpty || !rawSchedule.isObject) {
            return Schedule.EMPTY
        }

        val openingHours = rawSchedule.get("opening_hours")?.asText()?.trim().orEmpty()
        if (openingHours.isEmpty()) {
            return Schedule.EMPTY
        }

        if (openingHours == "24/7") {
            return Schedule(emptyMap(), is24x7 = true)
        }

        val match = simpleDailyRangeRegex.matchEntire(openingHours) ?: return Schedule.EMPTY
        val from = parseTimeOrNull(match.groupValues[1]) ?: return Schedule.EMPTY
        val to = parseTimeOrNull(match.groupValues[2]) ?: return Schedule.EMPTY
        val interval = WorkingInterval(from, to)
        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)
        Weekday.entries.forEach { weekday ->
            days[weekday] = DaySchedule(listOf(interval))
        }
        return Schedule(days)
    }

    private fun parseTimeOrNull(value: String): LocalTime? {
        return if (value == "24:00") {
            LocalTime.MIDNIGHT
        } else {
            runCatching { LocalTime.parse(value) }.getOrNull()
        }
    }
}
