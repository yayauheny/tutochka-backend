package yayauheny.by.model.schedule

/**
 * Schedule for a single day with working hours intervals
 */
data class DaySchedule(
    val workingHours: List<WorkingInterval>
)
