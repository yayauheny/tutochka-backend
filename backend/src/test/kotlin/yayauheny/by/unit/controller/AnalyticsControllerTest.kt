package yayauheny.by.unit.controller

import java.math.BigDecimal
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.model.analytics.AnalyticsEventCommand
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.model.analytics.ProductAnalyticsEvent
import yayauheny.by.helpers.testPost

@WebMvcTest(AnalyticsController::class)
@DisplayName("AnalyticsController Tests")
class AnalyticsControllerTest : RoutingTestBase() {
    @Test
    @DisplayName("GIVEN analytics event request WHEN POST /api/v1/analytics/events THEN track event with headers")
    fun given_analytics_event_request_when_post_events_then_track_event_with_headers() {
        withRoutingApp {
            val response =
                testPost(
                    "/api/v1/analytics/events",
                    """{"event":"restroom_details_opened","lat":53.9,"lon":27.5,"resultsCount":2,"durationMs":150,"metadata":{"source":"test"}}""",
                    mapOf(
                        "X-Tg-User-Id" to "12345",
                        "X-Tg-Chat-Id" to "chat-1",
                        "X-Tg-Username" to " user ",
                        "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
                    )
                )

            assertThat(response.status).isEqualTo(HttpStatus.ACCEPTED)

            verify(analyticsService).trackProductEvent(
                AnalyticsEventCommand(
                    event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED,
                    tgUserId = 12345L,
                    tgChatId = "chat-1",
                    username = "user",
                    source = AnalyticsSource.TELEGRAM_BOT,
                    lat = BigDecimal("53.90"),
                    lon = BigDecimal("27.50"),
                    resultsCount = 2,
                    durationMs = 150,
                    acquisitionTag = null,
                    metadata = mapOf("source" to "test")
                )
            )
        }
    }

    @Test
    @DisplayName("GIVEN invalid source header WHEN POST /api/v1/analytics/events THEN default to API")
    fun given_invalid_source_header_when_post_events_then_default_to_api() {
        withRoutingApp {
            val response =
                testPost(
                    "/api/v1/analytics/events",
                    """{"event":"route_clicked"}""",
                    mapOf("X-Analytics-Source" to "unknown")
                )

            assertThat(response.status).isEqualTo(HttpStatus.ACCEPTED)

            verify(analyticsService).trackProductEvent(
                AnalyticsEventCommand(
                    event = ProductAnalyticsEvent.ROUTE_CLICKED,
                    tgUserId = null,
                    tgChatId = null,
                    username = null,
                    source = AnalyticsSource.API,
                    lat = null,
                    lon = null,
                    resultsCount = null,
                    durationMs = null,
                    acquisitionTag = null,
                    metadata = emptyMap()
                )
            )
        }
    }

    @Test
    @DisplayName("GIVEN enum style uppercase source header WHEN POST /api/v1/analytics/events THEN preserve analytics source")
    fun given_enum_style_uppercase_source_header_when_post_events_then_preserve_source() {
        withRoutingApp {
            val response =
                testPost(
                    "/api/v1/analytics/events",
                    """{"event":"route_clicked"}""",
                    mapOf("X-Analytics-Source" to "TELEGRAM_BOT")
                )

            assertThat(response.status).isEqualTo(HttpStatus.ACCEPTED)

            verify(analyticsService).trackProductEvent(
                AnalyticsEventCommand(
                    event = ProductAnalyticsEvent.ROUTE_CLICKED,
                    tgUserId = null,
                    tgChatId = null,
                    username = null,
                    source = AnalyticsSource.TELEGRAM_BOT,
                    lat = null,
                    lon = null,
                    resultsCount = null,
                    durationMs = null,
                    acquisitionTag = null,
                    metadata = emptyMap()
                )
            )
        }
    }
}
