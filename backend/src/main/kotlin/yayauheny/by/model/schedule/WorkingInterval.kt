package yayauheny.by.model.schedule

import java.time.LocalTime

/**
 * Working time interval (from - to)
 */
data class WorkingInterval(
    val from: LocalTime,
    val to: LocalTime
)
