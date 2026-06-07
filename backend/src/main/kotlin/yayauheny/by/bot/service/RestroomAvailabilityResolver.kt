package yayauheny.by.bot.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Clock
import java.time.ZoneId
import org.springframework.stereotype.Component
import yayauheny.by.bot.util.WorkTimeFormatter
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.util.ScheduleUtils

@Component
class RestroomAvailabilityResolver(
    private val scheduleMappingService: ScheduleMappingService,
    private val clock: Clock
) {
    private val objectMapper = jacksonObjectMapper()
    private val zoneId = ZoneId.of("Europe/Minsk")

    fun resolve(restroom: RestroomResponseDto): ResolvedWorkTime {
        val workTime = effectiveWorkTime(restroom)
            ?: return ResolvedWorkTime(AvailabilityState.NONE, "🟡 Время работы неизвестно", "Время работы не указано")

        val rawDisplay = WorkTimeFormatter.formatWorkTime(workTime)
        val provider = effectiveProvider(restroom.originProvider, workTime)
        val schedule = runCatching {
            scheduleMappingService.mapSchedule(provider, objectMapper.valueToTree(workTime))
        }.getOrNull()

        if (schedule == null || schedule.isEmpty()) {
            return ResolvedWorkTime(
                status = AvailabilityState.UNKNOWN,
                statusText = "🟡 Время работы неизвестно",
                workTimeText = rawDisplay
            )
        }

        val statusText = if (ScheduleUtils.isOpenAt(schedule, clock.instant(), zoneId)) {
            "🟢 Туалет открыт сейчас"
        } else {
            "🔴 Туалет закрыт"
        }

        return ResolvedWorkTime(
            status = if (statusText.startsWith("🟢")) AvailabilityState.OPEN else AvailabilityState.CLOSED,
            statusText = statusText,
            workTimeText = WorkTimeFormatter.formatSchedule(schedule)
        )
    }

    private fun effectiveWorkTime(restroom: RestroomResponseDto): Map<String, Any>? {
        val building = restroom.building
        if (restroom.inheritBuildingSchedule && building?.workTime?.isNotEmpty() == true) {
            return building.workTime
        }

        return restroom.workTime?.takeIf { it.isNotEmpty() }
    }

    private fun effectiveProvider(
        originProvider: ImportProvider,
        workTime: Map<String, Any>
    ): ImportProvider {
        if (originProvider == ImportProvider.USER || originProvider == ImportProvider.MANUAL) {
            if (workTime.containsKey("is_24x7") || workTime.keys.any { it in setOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun", "mon", "tue", "wed", "thu", "fri", "sat", "sun") }) {
                return ImportProvider.TWO_GIS
            }
        }
        return originProvider
    }
}

enum class AvailabilityState {
    OPEN,
    CLOSED,
    UNKNOWN,
    NONE
}

data class ResolvedWorkTime(
    val status: AvailabilityState,
    val statusText: String,
    val workTimeText: String
)
