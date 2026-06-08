package yayauheny.by.unit.analytics.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.model.AnalyticsTrackRequestedEvent
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.BusinessAnalyticsEventListener
import yayauheny.by.analytics.service.BusinessAnalyticsMetrics
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

class BusinessAnalyticsEventListenerTest {
    private val analyticsService = mockk<AnalyticsService>()
    private val businessAnalyticsMetrics = mockk<BusinessAnalyticsMetrics>(relaxed = true)
    private val listener = BusinessAnalyticsEventListener(analyticsService, businessAnalyticsMetrics)

    @Test
    fun `handle increments recorded metric when analytics event is stored`() {
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.BOT_STARTED,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user",
                source = AnalyticsSource.TELEGRAM_BOT
            )
        every { analyticsService.trackProductEvent(command) } returns true

        listener.handle(AnalyticsTrackRequestedEvent(command))

        verify { businessAnalyticsMetrics.incrementRecorded(ProductAnalyticsEvent.BOT_STARTED, AnalyticsSource.TELEGRAM_BOT.value) }
        verify(exactly = 0) { businessAnalyticsMetrics.incrementFailure(any(), any()) }
    }

    @Test
    fun `handle increments failure metric when analytics event is not trackable`() {
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
                tgUserId = null,
                tgChatId = null,
                username = null,
                source = AnalyticsSource.TELEGRAM_BOT
            )
        every { analyticsService.trackProductEvent(command) } returns false

        listener.handle(AnalyticsTrackRequestedEvent(command))

        verify { businessAnalyticsMetrics.incrementFailure(ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED, "not_trackable") }
        verify(exactly = 0) { businessAnalyticsMetrics.incrementRecorded(any(), any()) }
    }

    @Test
    fun `handle swallows persistence exceptions and increments failure metric`() {
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user",
                source = AnalyticsSource.TELEGRAM_BOT
            )
        every { analyticsService.trackProductEvent(command) } throws RuntimeException("boom")

        listener.handle(AnalyticsTrackRequestedEvent(command))

        verify { businessAnalyticsMetrics.incrementFailure(ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS, "persistence") }
        verify(exactly = 0) { businessAnalyticsMetrics.incrementRecorded(any(), any()) }
    }
}
