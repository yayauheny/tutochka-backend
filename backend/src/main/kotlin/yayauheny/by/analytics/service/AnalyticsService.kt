package yayauheny.by.analytics.service

import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.model.EncryptedAnalyticsEventCommand
import yayauheny.by.analytics.repository.AnalyticsRepository
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.util.EncryptionService
import yayauheny.by.util.toBigDecimalRounded

class AnalyticsService(
    private val analyticsRepository: AnalyticsRepository,
    private val encryptionService: EncryptionService
) {
    suspend fun trackProductEvent(command: AnalyticsEventCommand) {
        when {
            command.hasTelegramIdentity() -> analyticsRepository.trackProductEvent(command.toEncryptedCommand())
            command.canBeTrackedAnonymously() -> analyticsRepository.trackProductEvent(command.toAnonymousCommand())
        }
    }

    suspend fun trackNearestRestroomsSearch(
        identity: AnalyticsIdentityHeaders,
        resultsCount: Int,
        lat: Double,
        lon: Double
    ) {
        val event =
            if (resultsCount > 0) {
                ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED
            } else {
                ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS
            }

        val command =
            AnalyticsEventCommand(
                event = event,
                tgUserId = identity.tgUserId,
                tgChatId = identity.tgChatId,
                username = identity.username,
                source = identity.source,
                lat = lat.toBigDecimalRounded(),
                lon = lon.toBigDecimalRounded(),
                resultsCount = resultsCount
            )
        trackProductEvent(command)
    }

    private fun AnalyticsEventCommand.hasTelegramIdentity(): Boolean = tgUserId != null && !tgChatId.isNullOrBlank()

    private fun AnalyticsEventCommand.canBeTrackedAnonymously(): Boolean =
        source == AnalyticsSource.API && tgUserId == null && tgChatId.isNullOrBlank()

    private fun AnalyticsEventCommand.toEncryptedCommand(): EncryptedAnalyticsEventCommand {
        return EncryptedAnalyticsEventCommand(
            event = event,
            tgUserId = encryptionService.encrypt(requireNotNull(tgUserId).toString()),
            tgChatId = encryptionService.encrypt(requireNotNull(tgChatId)),
            username = username,
            source = source,
            lat = lat,
            lon = lon,
            resultsCount = resultsCount,
            durationMs = durationMs,
            metadata = metadata
        )
    }

    private fun AnalyticsEventCommand.toAnonymousCommand(): EncryptedAnalyticsEventCommand {
        return EncryptedAnalyticsEventCommand(
            event = event,
            tgUserId = null,
            tgChatId = null,
            username = null,
            source = source,
            lat = lat,
            lon = lon,
            resultsCount = resultsCount,
            durationMs = durationMs,
            metadata = metadata
        )
    }
}
