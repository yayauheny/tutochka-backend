package yayauheny.by.unit.controller

import java.time.Instant
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.HttpStatus
import yayauheny.by.analytics.api.PublicAnalyticsController
import yayauheny.by.analytics.model.PublicAnalyticsDailySeriesPoint
import yayauheny.by.analytics.model.PublicAnalyticsEventTotal
import yayauheny.by.analytics.model.PublicAnalyticsHeadlineKpi
import yayauheny.by.analytics.model.PublicAnalyticsMonthlySeriesPoint
import yayauheny.by.analytics.model.PublicAnalyticsResponse
import yayauheny.by.analytics.model.PublicAnalyticsSummary
import yayauheny.by.analytics.model.PublicAnalyticsTrend
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.assertBodyContains
import yayauheny.by.helpers.testGet

@WebMvcTest(PublicAnalyticsController::class)
@DisplayName("PublicAnalyticsController Tests")
class PublicAnalyticsControllerTest : RoutingTestBase() {
    @Test
    @DisplayName("GIVEN backend routing WHEN GET root THEN return not found")
    fun given_backend_routing_when_get_root_then_return_not_found() {
        withRoutingApp {
            val response = testGet("/")

            assertEquals(HttpStatus.NOT_FOUND, response.status)
        }
    }

    @Test
    @DisplayName("GIVEN public analytics route WHEN GET endpoint THEN return safe JSON aggregates")
    fun given_public_analytics_route_when_get_endpoint_then_return_safe_json_aggregates() {
        given(publicAnalyticsService.getPublicAnalytics()).willReturn(sampleResponse())

        withRoutingApp {
            val response = testGet("/api/v1/public/analytics")

            assertEquals(HttpStatus.OK, response.status)
            response.assertJsonContentType()
            val body = response.bodyAsText()
            assertTrue(body.contains("\"timezone\":\"Europe/Minsk\""))
            assertTrue(body.contains("\"headlineKpis\""))
            assertTrue(body.contains("\"dailySeries\""))
            assertFalse(body.contains("tg_user_id"))
            assertFalse(body.contains("tg_chat_id"))
            assertFalse(body.contains("username"))
            assertFalse(body.contains("\"lat\""))
            assertFalse(body.contains("\"lon\""))
            assertFalse(body.contains("metadata"))
        }
    }

    private fun sampleResponse() =
        PublicAnalyticsResponse(
            generatedAt = Instant.parse("2026-05-15T09:00:00Z"),
            timezone = "Europe/Minsk",
            headlineKpis =
                listOf(
                    PublicAnalyticsHeadlineKpi(
                        key = "new_users_30d",
                        label = "New users",
                        value = 40.0,
                        period = "Last 30 days",
                        changePct = 12.5,
                        trend = PublicAnalyticsTrend.UP
                    )
                ),
            summary =
                PublicAnalyticsSummary(
                    totalUsers = 120,
                    newUsersToday = 2,
                    newUsers7d = 9,
                    newUsers30d = 40,
                    dailyActiveUsers = 18,
                    monthlyActiveUsers = 70,
                    totalSearches = 210,
                    searchesToday = 8,
                    searches7d = 42,
                    searches30d = 101,
                    successfulSearches = 180,
                    emptySearches = 30,
                    successRate = 85.71,
                    averageResultsPerSearch = 2.35,
                    detailsOpened = 28
                ),
            dailySeries =
                listOf(
                    PublicAnalyticsDailySeriesPoint("2026-05-14", 12, 20, 18, 2, 6),
                    PublicAnalyticsDailySeriesPoint("2026-05-15", 18, 22, 19, 3, 7)
                ),
            monthlySeries =
                listOf(
                    PublicAnalyticsMonthlySeriesPoint("2026-04", 16, 48, 71),
                    PublicAnalyticsMonthlySeriesPoint("2026-05", 24, 70, 101)
                ),
            eventTotals =
                listOf(
                    PublicAnalyticsEventTotal("restroom_details_opened", "Details opened", 28)
                )
        )
}
