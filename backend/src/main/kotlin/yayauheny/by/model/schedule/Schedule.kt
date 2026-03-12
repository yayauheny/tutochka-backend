package yayauheny.by.model.schedule

/**
 * Canonical schedule format - our internal domain model
 * This format is provider-agnostic and used throughout the application
 */
data class Schedule(
    val days: Map<Weekday, DaySchedule>,
    val is24x7: Boolean = false
) {
    companion object {
        val EMPTY = Schedule(emptyMap(), false)
    }

    fun isEmpty(): Boolean = days.isEmpty() && !is24x7

    fun hasDay(weekday: Weekday): Boolean = days.containsKey(weekday)
}
