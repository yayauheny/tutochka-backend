package integration.api.analytics

import java.math.BigDecimal
import java.util.UUID
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.common.config.EncryptionProperties
import yayauheny.by.model.analytics.AnalyticsSource
import yayauheny.by.testsupport.TEST_ENCRYPTION_KEYSET_JSON
import yayauheny.by.util.EncryptionService

@Tag("integration")
class AnalyticsApiTest : integration.base.BaseIntegrationTest() {
    private val encryptionService = EncryptionService(EncryptionProperties(keyset = EncryptionProperties.Keyset(TEST_ENCRYPTION_KEYSET_JSON)))

    @Test
    @DisplayName("POST /api/v1/analytics/events stores encrypted user and event data")
    fun post_event_stores_encrypted_user_and_event_data() {
        val response =
            mockMvc.testPost(
                "/api/v1/analytics/events",
                """{"event":"restroom_details_opened","metadata":{"restroom_id":"restroom-123"}}""",
                mapOf(
                    "X-Tg-User-Id" to "10",
                    "X-Tg-Chat-Id" to "123",
                    "X-Tg-Username" to "test-user-10",
                    "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
                )
            )

        assertThat(response.status.value()).isEqualTo(202)

        val user = userRepository.findAll().single()
        assertThat(encryptionService.decrypt(user.tgUserId)).isEqualTo("10")
        assertThat(encryptionService.decrypt(user.tgChatId)).isEqualTo("123")
        assertThat(user.username).isEqualTo("test-user-10")

        val userAnalytics = userAnalyticsRepository.findAll().single()
        assertThat(userAnalytics.detailsOpenedCount).isEqualTo(1)
        assertThat(userAnalytics.lastEvent).isEqualTo("restroom_details_opened")

        val event = analyticsEventRepository.findAll().single()
        assertThat(event.event).isEqualTo("restroom_details_opened")
        assertThat(event.metadata).containsEntry("restroom_id", "restroom-123")
    }

    @Test
    @DisplayName("POST /api/v1/analytics/events keeps separate users for different tg ids")
    fun post_event_keeps_separate_users_for_different_tg_ids() {
        val body = """{"event":"restroom_details_opened"}"""

        mockMvc.testPost(
            "/api/v1/analytics/events",
            body,
            mapOf(
                "X-Tg-User-Id" to "10",
                "X-Tg-Chat-Id" to "123",
                "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
            )
        )
        mockMvc.testPost(
            "/api/v1/analytics/events",
            body,
            mapOf(
                "X-Tg-User-Id" to "11",
                "X-Tg-Chat-Id" to "123",
                "X-Analytics-Source" to AnalyticsSource.TELEGRAM_BOT.value
            )
        )

        assertThat(userRepository.count()).isEqualTo(2)
        assertThat(userAnalyticsRepository.count()).isEqualTo(2)
        assertThat(analyticsEventRepository.count()).isEqualTo(2)
    }

    @Test
    @DisplayName("GET /api/v1/restrooms/nearest stores successful nearest-restroom analytics")
    fun nearest_restrooms_success_stores_analytics() {
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

        val response =
            mockMvc.testGet(
                "/api/v1/restrooms/nearest",
                mapOf(
                    "lat" to "53.904321",
                    "lon" to "27.561234",
                    "limit" to "5",
                    "distanceMeters" to "1000"
                ),
                mapOf(
                    "X-Tg-User-Id" to "10",
                    "X-Tg-Chat-Id" to "123",
                    "X-Tg-Username" to "test-user-10"
                )
            )

        assertThat(response.status.value()).isEqualTo(200)
        assertThat(response.bodyAsText()).contains("Analytics restroom")

        val event = analyticsEventRepository.findAll().single()
        assertThat(event.event).isEqualTo("nearest_restrooms_returned")
        assertThat(event.lat).isEqualByComparingTo(BigDecimal("53.90"))
        assertThat(event.lon).isEqualByComparingTo(BigDecimal("27.56"))
        assertThat(event.resultsCount).isEqualTo(1)

        val userAnalytics = userAnalyticsRepository.findAll().single()
        assertThat(userAnalytics.searchesCount).isEqualTo(1)
        assertThat(userAnalytics.successfulSearchesCount).isEqualTo(1)
        assertThat(userAnalytics.emptySearchesCount).isEqualTo(0)
    }

    @Test
    @DisplayName("GET /api/v1/restrooms/nearest stores empty-search analytics when no restrooms match")
    fun nearest_restrooms_empty_result_stores_analytics() {
        val response =
            mockMvc.testGet(
                "/api/v1/restrooms/nearest",
                mapOf(
                    "lat" to "53.904321",
                    "lon" to "27.561234",
                    "limit" to "5",
                    "distanceMeters" to "1000"
                ),
                mapOf(
                    "X-Tg-User-Id" to "10",
                    "X-Tg-Chat-Id" to "123",
                    "X-Tg-Username" to "test-user-10"
                )
            )

        assertThat(response.status.value()).isEqualTo(200)
        assertThat(response.bodyAsText()).isEqualTo("[]")

        val event = analyticsEventRepository.findAll().single()
        assertThat(event.event).isEqualTo("nearest_restrooms_no_results")
        assertThat(event.lat).isEqualByComparingTo(BigDecimal("53.90"))
        assertThat(event.lon).isEqualByComparingTo(BigDecimal("27.56"))
        assertThat(event.resultsCount).isEqualTo(0)

        val userAnalytics = userAnalyticsRepository.findAll().single()
        assertThat(userAnalytics.searchesCount).isEqualTo(1)
        assertThat(userAnalytics.successfulSearchesCount).isEqualTo(0)
        assertThat(userAnalytics.emptySearchesCount).isEqualTo(1)
    }
}
