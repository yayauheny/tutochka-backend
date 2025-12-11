package yayauheny.by.model.schedule

/**
 * Day of the week enum for schedule representation
 */
enum class Weekday {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN;

    companion object {
        fun fromTwoGisKey(key: String): Weekday {
            return when (key) {
                "Mon" -> MON
                "Tue" -> TUE
                "Wed" -> WED
                "Thu" -> THU
                "Fri" -> FRI
                "Sat" -> SAT
                "Sun" -> SUN
                else -> throw IllegalArgumentException("Unknown weekday key: $key")
            }
        }
    }
}
