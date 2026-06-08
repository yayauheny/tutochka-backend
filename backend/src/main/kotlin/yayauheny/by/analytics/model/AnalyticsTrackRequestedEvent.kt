package yayauheny.by.analytics.model

import yayauheny.by.model.analytics.AnalyticsEventCommand

data class AnalyticsTrackRequestedEvent(
    val command: AnalyticsEventCommand
)
