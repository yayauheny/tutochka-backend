package integration.api.analytics

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.time.Instant
import java.time.ZoneId
import java.time.YearMonth
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testJson
import yayauheny.by.model.analytics.ProductAnalyticsEvent

@Tag("integration")
class PublicAnalyticsApiTest : BaseIntegrationTest() {
    private val zoneId = ZoneId.of("Europe/Minsk")

    @Test
    @DisplayName("GIVEN seeded analytics WHEN GET public analytics THEN return grouped safe dashboard data")
    fun given_seeded_analytics_when_get_public_analytics_then_return_grouped_safe_dashboard_data() =
        runTest {
            val today = Instant.now().atZone(zoneId).toLocalDate()

            val userToday =
                DatabaseTestHelper.insertAnalyticsUser(
                    dslContext,
                    DatabaseTestHelper.createTestAnalyticsUserData(
                        createdAt = atNoon(today)
                    )
                )
            val userRecent =
                DatabaseTestHelper.insertAnalyticsUser(
                    dslContext,
                    DatabaseTestHelper.createTestAnalyticsUserData(
                        createdAt = atNoon(today.minusDays(2))
                    )
                )
            val userMonth =
                DatabaseTestHelper.insertAnalyticsUser(
                    dslContext,
                    DatabaseTestHelper.createTestAnalyticsUserData(
                        createdAt = atNoon(today.minusDays(10))
                    )
                )
            val userOld =
                DatabaseTestHelper.insertAnalyticsUser(
                    dslContext,
                    DatabaseTestHelper.createTestAnalyticsUserData(
                        createdAt = atNoon(today.minusDays(40))
                    )
                )

            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value,
                    userId = userToday,
                    resultsCount = 3,
                    createdAt = atNoon(today)
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.NEAREST_RESTROOMS_NO_RESULTS.value,
                    userId = userRecent,
                    resultsCount = 0,
                    createdAt = atNoon(today)
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.ROUTE_CLICKED.value,
                    userId = userToday,
                    createdAt = atNoon(today)
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value,
                    userId = userToday,
                    createdAt = atNoon(today)
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value,
                    userId = userRecent,
                    resultsCount = 1,
                    createdAt = atNoon(today.minusDays(1))
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.RESTROOM_DETAILS_OPENED.value,
                    userId = userMonth,
                    createdAt = atNoon(today.minusDays(10))
                )
            )
            DatabaseTestHelper.insertAnalyticsEvent(
                dslContext,
                DatabaseTestHelper.createTestAnalyticsEventData(
                    event = ProductAnalyticsEvent.NEAREST_RESTROOMS_RETURNED.value,
                    userId = userOld,
                    resultsCount = 2,
                    createdAt = atNoon(today.minusDays(40))
                )
            )
            repeat(10) { index ->
                DatabaseTestHelper.insertAnalyticsEvent(
                    dslContext,
                    DatabaseTestHelper.createTestAnalyticsEventData(
                        event = ProductAnalyticsEvent.ROUTE_CLICKED.value,
                        userId = userToday,
                        createdAt = atNoon(today.minusDays(2)).plusSeconds(index.toLong())
                    )
                )
            }

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.get("/api/v1/public/analytics")

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()

                val body = response.bodyAsText()
                assertFalse(body.contains("tg_user_id"))
                assertFalse(body.contains("tg_chat_id"))
                assertFalse(body.contains("username"))
                assertFalse(body.contains("\"lat\""))
                assertFalse(body.contains("\"lon\""))
                assertFalse(body.contains("metadata"))

                val json = testJson.parseToJsonElement(body).jsonObject
                val summary = json.getValue("summary").jsonObject

                assertEquals(
                    4,
                    summary
                        .getValue("totalUsers")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    1,
                    summary
                        .getValue("newUsersToday")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    2,
                    summary
                        .getValue("newUsers7d")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    3,
                    summary
                        .getValue("newUsers30d")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    2,
                    summary
                        .getValue("dailyActiveUsers")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    3,
                    summary
                        .getValue("monthlyActiveUsers")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    4,
                    summary
                        .getValue("totalSearches")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    2,
                    summary
                        .getValue("searchesToday")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    3,
                    summary
                        .getValue("searches7d")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    3,
                    summary
                        .getValue("searches30d")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    3,
                    summary
                        .getValue("successfulSearches")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    1,
                    summary
                        .getValue("emptySearches")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals("75.0", summary.getValue("successRate").jsonPrimitive.content)
                assertEquals("1.5", summary.getValue("averageResultsPerSearch").jsonPrimitive.content)
                assertEquals(
                    11,
                    summary
                        .getValue("routeClicks")
                        .jsonPrimitive.content
                        .toLong()
                )
                assertEquals(
                    2,
                    summary
                        .getValue("detailsOpened")
                        .jsonPrimitive.content
                        .toLong()
                )

                val dailySeries = json.getValue("dailySeries").jsonArray
                assertEquals(30, dailySeries.size)
                assertTrue(
                    dailySeries.any { point ->
                        val obj = point.jsonObject
                        obj.getValue("date").jsonPrimitive.content == today.toString() &&
                            obj
                                .getValue("searches")
                                .jsonPrimitive.content
                                .toLong() == 2L
                    }
                )

                val eventTotals = json.getValue("eventTotals").jsonArray
                assertEquals(1, eventTotals.size)
                val routeRow = eventTotals.first().jsonObject
                assertEquals(ProductAnalyticsEvent.ROUTE_CLICKED.value, routeRow.getValue("eventKey").jsonPrimitive.content)
                assertEquals("Route opened", routeRow.getValue("label").jsonPrimitive.content)
                assertEquals(
                    11,
                    routeRow
                        .getValue("count")
                        .jsonPrimitive.content
                        .toLong()
                )

                val monthlySeries = json.getValue("monthlySeries").jsonArray
                assertEquals(6, monthlySeries.size)
                val currentMonth = YearMonth.from(today).toString()
                assertTrue(
                    monthlySeries.any { item ->
                        val obj = item.jsonObject
                        obj.getValue("month").jsonPrimitive.content == currentMonth
                    }
                )
            }
        }

    private fun atNoon(date: java.time.LocalDate): Instant = date.atTime(12, 0).atZone(zoneId).toInstant()
}
