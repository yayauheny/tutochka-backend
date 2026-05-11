package yayauheny.by.analytics.repository

import yayauheny.by.analytics.model.EncryptedAnalyticsEventCommand

interface AnalyticsRepository {
    suspend fun trackProductEvent(command: EncryptedAnalyticsEventCommand)
}
