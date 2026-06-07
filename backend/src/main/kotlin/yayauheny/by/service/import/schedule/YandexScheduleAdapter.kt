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
class YandexScheduleAdapter : ScheduleAdapter {
    override fun supports(provider: ImportProvider): Boolean {
        return provider == ImportProvider.YANDEX_MAPS
    }

    override fun toSchedule(rawSchedule: JsonNode?): Schedule {
        if (rawSchedule == null || rawSchedule.isEmpty || !rawSchedule.isObject) {
            return Schedule.EMPTY
        }

        val days = EnumMap<Weekday, DaySchedule>(Weekday::class.java)

        rawSchedule.fields().forEach { (rawDay, dayNode) ->
            val weekday = rawDay.toWeekdayOrNull() ?: return@forEach
            if (!dayNode.isObject) return@forEach

            val workingHours = parseWorkingHours(dayNode)
            if (workingHours.isNotEmpty()) {
                days[weekday] = DaySchedule(workingHours)
            }
        }

        return Schedule(days)
    }

    private fun parseWorkingHours(dayNode: JsonNode): List<WorkingInterval> {
        val intervalsNode = dayNode.get("intervals")
        if (intervalsNode != null && intervalsNode.isArray) {
            val intervals = intervalsNode.mapNotNull { intervalNode ->
                parseInterval(intervalNode.get("from")?.asText(), intervalNode.get("to")?.asText())
            }
            if (intervals.isNotEmpty()) {
                return intervals
            }
        }

        return listOfNotNull(parseInterval(dayNode.get("from")?.asText(), dayNode.get("to")?.asText()))
    }

    private fun parseInterval(
        from: String?,
        to: String?
    ): WorkingInterval? {
        if (from.isNullOrBlank() || to.isNullOrBlank()) {
            return null
        }

        val fromTime = parseTimeOrNull(from) ?: return null
        val toTime = parseTimeOrNull(to) ?: return null
        return WorkingInterval(fromTime, toTime)
    }

    private fun parseTimeOrNull(value: String): LocalTime? {
        return if (value == "24:00") {
            LocalTime.MIDNIGHT
        } else {
            runCatching { LocalTime.parse(value) }.getOrNull()
        }
    }

    private fun String.toWeekdayOrNull(): Weekday? {
        return when (lowercase()) {
            "mon" -> Weekday.MON
            "tue" -> Weekday.TUE
            "wed" -> Weekday.WED
            "thu" -> Weekday.THU
            "fri" -> Weekday.FRI
            "sat" -> Weekday.SAT
            "sun" -> Weekday.SUN
            else -> null
        }
    }
}
