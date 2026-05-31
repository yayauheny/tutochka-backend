package integration.api.restrooms

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.micrometer.prometheusmetrics.PrometheusConfig
import integration.base.BaseIntegrationTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import yayauheny.by.analytics.model.AnalyticsIdentityHeaders
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.helpers.any
import yayauheny.by.helpers.anyDouble
import yayauheny.by.helpers.anyInt
import yayauheny.by.helpers.testGet
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.service.RestroomService
import java.util.UUID
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doThrow
import yayauheny.by.Application

@SpringBootTest(classes = [Application::class])
@Tag("integration")
@Import(RestroomMetricsIntegrationTest.MetricsTestConfiguration::class)
class RestroomMetricsIntegrationTest : BaseIntegrationTest() {
    @MockBean
    private lateinit var restroomService: RestroomService

    @MockBean
    private lateinit var analyticsService: AnalyticsService

    @org.springframework.beans.factory.annotation.Autowired
    private lateinit var meterRegistry: PrometheusMeterRegistry

    @TestConfiguration
    class MetricsTestConfiguration {
        @Bean
        fun prometheusMeterRegistry(): PrometheusMeterRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }

    @Test
    fun nearest_endpoint_should_record_custom_search_metrics_and_expose_them_on_scrape_endpoint() {
        given(restroomService.findNearestRestrooms(53.9, 27.56, 5, 300)).willReturn(
            listOf(
                NearestRestroomSlimDto(
                    id = UUID.randomUUID(),
                    displayName = "Test restroom",
                    distanceMeters = 250.0,
                    feeType = FeeType.FREE,
                    queryCoordinates = Coordinates(53.9, 27.56),
                    restroomCoordinates = Coordinates(53.9005, 27.5605)
                )
            )
        )

        val nearestResponse =
            mockMvc.testGet(
                "/api/v1/restrooms/nearest",
                mapOf("lat" to "53.9", "lon" to "27.56", "distanceMeters" to "300", "limit" to "5"),
                headers = mapOf("X-Client-Type" to "telegram_bot")
            )
        assertThat(nearestResponse.status).isEqualTo(HttpStatus.OK)

        val body = meterRegistry.scrape()
        assertThat(body).contains("search_requests_total")
        assertThat(body).contains("client_type=\"telegram_bot\"")
        assertThat(body).contains("radius_bucket=\"0_300\"")
        assertThat(body).contains("search_results_total")
        assertThat(body).contains("result_bucket=\"1_2\"")
        assertThat(body).contains("search_quality_total")
        assertThat(body).contains("quality_bucket=\"poor\"")
        assertNoForbiddenMetricLabels(body)
    }

    @Test
    fun nearest_endpoint_invalid_query_params_should_still_record_request_metric_with_safe_radius_bucket() {
        val nearestResponse =
            mockMvc.testGet(
                "/api/v1/restrooms/nearest",
                mapOf("lat" to "abc", "lon" to "27.56", "distanceMeters" to "abc", "limit" to "5")
            )
        assertThat(nearestResponse.status).isEqualTo(HttpStatus.BAD_REQUEST)

        val body = meterRegistry.scrape()
        assertThat(body).contains("search_requests_total")
        assertThat(body).contains("radius_bucket=\"unknown\"")
        assertThat(body).contains("search_failures_total")
        assertThat(body).contains("reason=\"validation\"")
        assertNoForbiddenMetricLabels(body)
    }

    @Test
    fun nearest_endpoint_should_return_restrooms_even_when_analytics_tracking_fails() {
        given(restroomService.findNearestRestrooms(53.9, 27.56, 5, 300)).willReturn(
            listOf(
                NearestRestroomSlimDto(
                    id = UUID.randomUUID(),
                    displayName = "Analytics fallback restroom",
                    distanceMeters = 250.0,
                    feeType = FeeType.FREE,
                    queryCoordinates = Coordinates(53.9, 27.56),
                    restroomCoordinates = Coordinates(53.9005, 27.5605)
                )
            )
        )
        doThrow(RuntimeException("boom")).`when`(analyticsService)
            .trackNearestRestroomsSearch(any<AnalyticsIdentityHeaders>(), anyInt(), anyDouble(), anyDouble())

        val nearestResponse =
            mockMvc.testGet(
                "/api/v1/restrooms/nearest",
                mapOf("lat" to "53.9", "lon" to "27.56", "distanceMeters" to "300", "limit" to "5"),
                headers = mapOf(
                    "X-Client-Type" to "telegram_bot",
                    "X-Tg-User-Id" to "123",
                    "X-Tg-Chat-Id" to "chat-1"
                )
            )

        assertThat(nearestResponse.status).isEqualTo(HttpStatus.OK)
        assertThat(nearestResponse.bodyAsText()).contains("Analytics fallback restroom")
    }

    private fun assertNoForbiddenMetricLabels(body: String) {
        listOf(
            "lat=",
            "lon=",
            "user_id=",
            "userId=",
            "chat_id=",
            "restroom_id="
        ).forEach { fragment ->
            assertThat(body).doesNotContain(fragment)
        }
    }
}
