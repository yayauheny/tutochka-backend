package yayauheny.by.service.import.schedule

import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.schedule.Schedule

/**
 * Service for mapping provider-specific schedules to our canonical Schedule format
 */
class ScheduleMappingService(
    private val adapters: List<ScheduleAdapter>
) {
    /**
     * Convert provider-specific schedule to our canonical format
     * @param provider data provider (2ГИС, Yandex, Google Maps, OSM, etc.)
     * @param rawSchedule raw schedule JSON from provider
     * @return Schedule in our canonical format
     * @throws IllegalArgumentException if no adapter found for the provider
     */
    fun mapSchedule(
        provider: ImportProvider,
        rawSchedule: JsonObject?
    ): Schedule {
        val adapter =
            adapters.firstOrNull { it.supports(provider) }
                ?: throw IllegalArgumentException("No schedule adapter found for provider: $provider")

        return adapter.toSchedule(rawSchedule)
    }
}
