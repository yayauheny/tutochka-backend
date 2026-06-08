package yayauheny.by.unit.analytics.service

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.TestPropertySource
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.BusinessAnalyticsEventListener
import yayauheny.by.analytics.service.BusinessAnalyticsMetrics
import yayauheny.by.analytics.service.BusinessAnalyticsPublisher
import yayauheny.by.config.AnalyticsAsyncConfig
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@SpringBootTest(
    classes = [
        AnalyticsAsyncConfig::class,
        BusinessAnalyticsPublisher::class,
        BusinessAnalyticsEventListener::class,
        BusinessAnalyticsMetrics::class,
        BusinessAnalyticsAsyncFlowTest.TestConfig::class
    ],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@TestPropertySource(properties = ["analytics.async.enabled=true"])
class BusinessAnalyticsAsyncFlowTest {
    @Autowired
    private lateinit var businessAnalyticsPublisher: BusinessAnalyticsPublisher

    @Autowired
    private lateinit var meterRegistry: SimpleMeterRegistry

    @MockBean
    private lateinit var analyticsService: AnalyticsService

    @TestConfiguration
    class TestConfig {
        @Bean
        fun meterRegistry(): SimpleMeterRegistry = SimpleMeterRegistry()
    }

    @Test
    fun `async listener failure does not break publisher flow and records failure metric`() {
        val command =
            AnalyticsEventCommand(
                event = ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS,
                tgUserId = 10L,
                tgChatId = "123",
                username = "test-user",
                source = AnalyticsSource.TELEGRAM_BOT
            )
        given(analyticsService.trackProductEvent(command)).willThrow(RuntimeException("boom"))

        assertThatCode {
            businessAnalyticsPublisher.trackProductEvent(command)
        }.doesNotThrowAnyException()

        waitForCounter("business_analytics_failures_total", "event", ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value, "reason", "persistence")
    }

    private fun waitForCounter(
        meterName: String,
        firstTagKey: String,
        firstTagValue: String,
        secondTagKey: String,
        secondTagValue: String
    ) {
        repeat(20) {
            val count =
                meterRegistry
                    .find(meterName)
                    .tag(firstTagKey, firstTagValue)
                    .tag(secondTagKey, secondTagValue)
                    .counter()
                    ?.count()
                    ?: 0.0
            if (count > 0.0) {
                return
            }
            Thread.sleep(50)
        }

        throw AssertionError("Counter $meterName{$firstTagKey=$firstTagValue,$secondTagKey=$secondTagValue} was not incremented")
    }
}
