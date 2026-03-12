package yayauheny.by.service.import.schedule

import kotlinx.serialization.json.JsonObject
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.schedule.Schedule

/**
 * Interface for converting provider-specific schedule formats to our canonical Schedule format
 */
interface ScheduleAdapter {
    /**
     * Check if this adapter supports the given provider
     */
    fun supports(provider: ImportProvider): Boolean

    /**
     * Convert provider-specific schedule JSON to our canonical Schedule format
     * @param rawSchedule raw schedule JSON from provider
     * @return Schedule in our canonical format, or Schedule.EMPTY if schedule is null/empty
     */
    fun toSchedule(rawSchedule: JsonObject?): Schedule
}
