package integration.api.analytics

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.testJson
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.testsupport.TEST_ENCRYPTION_KEYSET_JSON
import yayauheny.by.tables.references.ANALYTICS_EVENTS
import yayauheny.by.tables.references.USER_ANALYTICS
import yayauheny.by.tables.references.USERS
import yayauheny.by.util.EncryptionService

@Tag("integration")
class AnalyticsApiTest : BaseIntegrationTest() {
    private val encryptionService = EncryptionService(TEST_ENCRYPTION_KEYSET_JSON)

    @Test
    @DisplayName("GIVEN analytics event WHEN POST /api/v1/analytics/events THEN store user and event data")
    fun given_analytics_event_when_post_analytics_events_then_store_user_and_event_data() =
        runTest {
            val body =
                buildJsonObject {
                    put("event", "restroom_details_opened")
                    put(
                        "metadata",
                        buildJsonObject {
                            put("restroom_id", "restroom-123")
                        }
                    )
                }.toString()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testPost(
                        "/api/v1/analytics/events",
                        body,
                        mapOf(
                            "X-Tg-User-Id" to "10",
                            "X-Tg-Chat-Id" to "123",
                            "X-Tg-Username" to "test-user-10",
                            "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
                        )
                    )

                assertEquals(HttpStatusCode.Accepted, response.status)
            }

            val user = dslContext.selectFrom(USERS).fetchOne() ?: error("Expected one analytics user")
            assertNotEquals("10", user.tgUserId)
            assertNotEquals("123", user.tgChatId)
            assertEquals("10", encryptionService.decrypt(requireNotNull(user.tgUserId)))
            assertEquals("123", encryptionService.decrypt(requireNotNull(user.tgChatId)))
            assertEquals("test-user-10", user.username)

            val userAnalytics = dslContext.selectFrom(USER_ANALYTICS).fetchOne() ?: error("Expected user analytics row")
            assertEquals(0, userAnalytics.searchesCount)
            assertEquals(0, userAnalytics.successfulSearchesCount)
            assertEquals(0, userAnalytics.emptySearchesCount)
            assertEquals(1, userAnalytics.detailsOpenedCount)
            assertEquals(0, userAnalytics.routesClickedCount)
            assertEquals("restroom_details_opened", userAnalytics.lastEvent)
            assertNotNull(userAnalytics.lastEventAt)

            val event = dslContext.selectFrom(ANALYTICS_EVENTS).fetchOne() ?: error("Expected analytics event")
            assertEquals("restroom_details_opened", event.event)
            assertEquals(null, event.lat)
            assertEquals(null, event.lon)
            assertEquals(1, dslContext.fetchCount(ANALYTICS_EVENTS))
            assertEquals(1, dslContext.fetchCount(USERS))
        }

    @Test
    @DisplayName("GIVEN same chat and different users WHEN POST analytics events THEN store both users")
    fun given_same_chat_and_different_users_when_post_analytics_events_then_store_both_users() =
        runTest {
            val body =
                buildJsonObject {
                    put("event", "restroom_details_opened")
                }.toString()

            KtorTestApplication.withApp(dslContext) { client ->
                client
                    .testPost(
                        "/api/v1/analytics/events",
                        body,
                        mapOf(
                            "X-Tg-User-Id" to "10",
                            "X-Tg-Chat-Id" to "123",
                            "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
                        )
                    ).also { response ->
                        assertEquals(HttpStatusCode.Accepted, response.status)
                    }

                client
                    .testPost(
                        "/api/v1/analytics/events",
                        body,
                        mapOf(
                            "X-Tg-User-Id" to "11",
                            "X-Tg-Chat-Id" to "123",
                            "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
                        )
                    ).also { response ->
                        assertEquals(HttpStatusCode.Accepted, response.status)
                    }
            }

            assertEquals(2, dslContext.fetchCount(USERS))
            assertEquals(2, dslContext.fetchCount(USER_ANALYTICS))
            assertEquals(2, dslContext.fetchCount(ANALYTICS_EVENTS))
        }

    @Test
    @DisplayName("GIVEN anonymous API analytics event WHEN POST analytics events THEN store event without user rows")
    fun given_anonymous_api_analytics_event_when_post_analytics_events_then_store_event_without_user_rows() =
        runTest {
            val body =
                buildJsonObject {
                    put("event", "restroom_details_opened")
                }.toString()

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/analytics/events", body)
                assertEquals(HttpStatusCode.Accepted, response.status)
            }

            val event = dslContext.selectFrom(ANALYTICS_EVENTS).fetchOne() ?: error("Expected analytics event")
            assertEquals("restroom_details_opened", event.event)
            assertEquals(AnalyticsSource.API.value, event.source)
            assertEquals(null, event.userId)
            assertEquals(1, dslContext.fetchCount(ANALYTICS_EVENTS))
            assertEquals(0, dslContext.fetchCount(USERS))
            assertEquals(0, dslContext.fetchCount(USER_ANALYTICS))
        }

    @Test
    @DisplayName("GIVEN malformed analytics JSON WHEN POST analytics events THEN return bad request and store nothing")
    fun given_malformed_analytics_json_when_post_analytics_events_then_return_bad_request_and_store_nothing() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/analytics/events", """{"event":""")
                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }

            assertEquals(0, dslContext.fetchCount(ANALYTICS_EVENTS))
            assertEquals(0, dslContext.fetchCount(USERS))
            assertEquals(0, dslContext.fetchCount(USER_ANALYTICS))
        }

    @Test
    @DisplayName("GIVEN invalid analytics event WHEN POST analytics events THEN return bad request and store nothing")
    fun given_invalid_analytics_event_when_post_analytics_events_then_return_bad_request_and_store_nothing() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testPost(
                        "/api/v1/analytics/events",
                        """{"event":"invalid_event"}"""
                    )
                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
            }

            assertEquals(0, dslContext.fetchCount(ANALYTICS_EVENTS))
            assertEquals(0, dslContext.fetchCount(USERS))
            assertEquals(0, dslContext.fetchCount(USER_ANALYTICS))
        }

    @Test
    @DisplayName("GIVEN analytics storage failure WHEN POST analytics events THEN return accepted")
    fun given_analytics_storage_failure_when_post_analytics_events_then_return_accepted() =
        runTest {
            val analyticsService = mockk<AnalyticsService>()
            coEvery { analyticsService.trackProductEvent(any()) } throws RuntimeException("boom")

            testApplication {
                application {
                    install(ContentNegotiation) {
                        json(testJson)
                    }
                    configureErrorHandling()
                    routing {
                        route("/api/v1") {
                            with(AnalyticsController(analyticsService)) {
                                analyticsRoutes()
                            }
                        }
                    }
                }
                val client = createClient { expectSuccess = false }
                val response =
                    client.post("/api/v1/analytics/events") {
                        contentType(ContentType.Application.Json)
                        setBody("""{"event":"restroom_details_opened"}""")
                    }

                assertEquals(HttpStatusCode.Accepted, response.status)
            }
        }

    @Test
    @DisplayName("GIVEN nearby restroom results WHEN GET /api/v1/restrooms/nearest THEN store success analytics")
    fun given_nearby_restroom_results_when_get_nearest_then_store_success_analytics() =
        runTest {
            val env = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                env.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Analytics restroom",
                    lat = 53.904321,
                    lon = 27.561234
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms/nearest") {
                        url {
                            parameters.append("lat", "53.904321")
                            parameters.append("lon", "27.561234")
                            parameters.append("limit", "5")
                            parameters.append("distanceMeters", "1000")
                        }
                        headers.append("X-Tg-User-Id", "10")
                        headers.append("X-Tg-Chat-Id", "123")
                        headers.append("X-Tg-Username", "test-user-10")
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                assertTrue(response.bodyAsText().contains("Analytics restroom"))
            }

            val event = dslContext.selectFrom(ANALYTICS_EVENTS).fetchOne() ?: error("Expected analytics event")
            assertEquals("nearest_restrooms_returned", event.event)
            assertEquals(BigDecimal("53.90"), event.lat)
            assertEquals(BigDecimal("27.56"), event.lon)
            assertEquals(1, event.resultsCount)

            val userAnalytics = dslContext.selectFrom(USER_ANALYTICS).fetchOne() ?: error("Expected user analytics row")
            assertEquals(1, userAnalytics.searchesCount)
            assertEquals(1, userAnalytics.successfulSearchesCount)
            assertEquals(0, userAnalytics.emptySearchesCount)
            assertEquals("nearest_restrooms_returned", userAnalytics.lastEvent)
        }

    @Test
    @DisplayName("GIVEN no nearby restrooms WHEN GET /api/v1/restrooms/nearest THEN store empty analytics")
    fun given_no_nearby_restrooms_when_get_nearest_then_store_empty_analytics() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms/nearest") {
                        url {
                            parameters.append("lat", "53.904321")
                            parameters.append("lon", "27.561234")
                            parameters.append("limit", "5")
                            parameters.append("distanceMeters", "1000")
                        }
                        headers.append("X-Tg-User-Id", "10")
                        headers.append("X-Tg-Chat-Id", "123")
                        headers.append("X-Tg-Username", "test-user-10")
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                assertEquals("[]", response.bodyAsText())
            }

            val event = dslContext.selectFrom(ANALYTICS_EVENTS).fetchOne() ?: error("Expected analytics event")
            assertEquals("nearest_restrooms_no_results", event.event)
            assertEquals(BigDecimal("53.90"), event.lat)
            assertEquals(BigDecimal("27.56"), event.lon)
            assertEquals(0, event.resultsCount)

            val userAnalytics = dslContext.selectFrom(USER_ANALYTICS).fetchOne() ?: error("Expected user analytics row")
            assertEquals(1, userAnalytics.searchesCount)
            assertEquals(0, userAnalytics.successfulSearchesCount)
            assertEquals(1, userAnalytics.emptySearchesCount)
            assertEquals("nearest_restrooms_no_results", userAnalytics.lastEvent)
        }
}
