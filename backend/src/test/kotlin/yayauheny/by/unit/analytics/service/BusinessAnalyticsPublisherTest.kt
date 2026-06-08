package yayauheny.by.unit.analytics.service

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.context.ApplicationEventPublisher
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.model.AnalyticsTrackRequestedEvent
import yayauheny.by.analytics.service.BusinessAnalyticsMetrics
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

class BusinessAnalyticsPublisherTest {
    private val applicationEventPublisher = mockk<ApplicationEventPublisher>()
    private val businessAnalyticsMetrics = mockk<BusinessAnalyticsMetrics>(relaxed = true)
    private val publisher = BusinessAnalyticsPublisher(applicationEventPublisher, businessAnalyticsMetrics)

    @Test
    fun `trackProductEvent publishes requested event`() {
        val eventSlot = slot<Any>()
        every { applicationEventPublisher.publishEvent(capture(eventSlot)) } just runs
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.BOT_STARTED,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user",
                source = AnalyticsSource.TELEGRAM_BOT
            )

        publisher.trackProductEvent(command)

        assertThat(eventSlot.captured).isEqualTo(AnalyticsTrackRequestedEvent(command))
    }

    @Test
    fun `trackNearestRestroomsSearch publishes nearest search event`() {
        val eventSlot = slot<Any>()
        every { applicationEventPublisher.publishEvent(capture(eventSlot)) } just runs

        publisher.trackNearestRestroomsSearch(
            AnalyticsIdentityHeaders(10L, "123", "test-user", AnalyticsSource.TELEGRAM_BOT),
            resultsCount = 0,
            lat = 53.9,
            lon = 27.56
        )

        val published = eventSlot.captured as AnalyticsTrackRequestedEvent
        assertThat(published.command.event).isEqualTo(ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS)
        assertThat(published.command.lat).isEqualTo(53.9)
        assertThat(published.command.lon).isEqualTo(27.56)
    }

    @Test
    fun `trackProductEvent swallows dispatch failures and records failure metric`() {
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.BOT_STARTED,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user",
                source = AnalyticsSource.TELEGRAM_BOT
            )
        every { applicationEventPublisher.publishEvent(any<AnalyticsTrackRequestedEvent>()) } throws RuntimeException("boom")

        publisher.trackProductEvent(command)

        verify { businessAnalyticsMetrics.incrementFailure(ProductAnalyticsEvent.BOT_STARTED, "dispatch") }
    }
}
