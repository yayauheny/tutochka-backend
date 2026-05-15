package integration.api.restrooms

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.testApplication
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.jooq.DSLContext
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.analytics.api.AnalyticsController
import yayauheny.by.analytics.api.PublicAnalyticsController
import yayauheny.by.config.configureMetrics
import yayauheny.by.config.configureRouting
import yayauheny.by.helpers.testJson
import yayauheny.by.importing.api.ImportController
import yayauheny.by.importing.service.ImportService
import yayauheny.by.metrics.BackendSearchMetrics
import yayauheny.by.analytics.service.AnalyticsService
import yayauheny.by.analytics.service.PublicAnalyticsService
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

@Tag("integration")
class RestroomMetricsIntegrationTest {
    @Test
    fun `nearest endpoint should record custom search metrics and expose them on scrape endpoint`() =
        testApplication {
            val restroomService = mockk<RestroomService>()
            val countryService = mockk<CountryService>(relaxed = true)
            val cityService = mockk<CityService>(relaxed = true)
            val importService = mockk<ImportService>(relaxed = true)
            val dslContext = mockk<DSLContext>(relaxed = true)
            val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            val backendSearchMetrics = BackendSearchMetrics(registry)

            coEvery {
                restroomService.findNearestRestrooms(53.9, 27.56, 5, 300)
            } returns
                listOf(
                    NearestRestroomSlimDto(
                        id = java.util.UUID.randomUUID(),
                        displayName = "Test restroom",
                        distanceMeters = 250.0,
                        feeType = FeeType.FREE,
                        queryCoordinates = Coordinates(53.9, 27.56),
                        restroomCoordinates = Coordinates(53.9005, 27.5605)
                    )
                )

            application {
                install(Koin) {
                    modules(
                        module {
                            single<CountryService> { countryService }
                            single<CityService> { cityService }
                            single<RestroomService> { restroomService }
                            single<ImportService> { importService }
                            single<DSLContext> { dslContext }
                            single<PrometheusMeterRegistry> { registry }
                            single<BackendSearchMetrics> { backendSearchMetrics }
                            single<AnalyticsService> { mockk(relaxed = true) }
                            single<PublicAnalyticsService> { mockk(relaxed = true) }
                        },
                        module {
                            single { yayauheny.by.controller.CountryController(get()) }
                            single { yayauheny.by.controller.CityController(get()) }
                            single { yayauheny.by.controller.RestroomController(get(), get(), get()) }
                            single { yayauheny.by.controller.HealthController(get()) }
                            single { ImportController(get()) }
                            single { AnalyticsController(get()) }
                            single { PublicAnalyticsController(get()) }
                        }
                    )
                }
                install(ContentNegotiation) { json(testJson) }
                configureMetrics(registry)
                configureErrorHandling()
                configureRouting()
            }

            val nearestResponse =
                client.get("/api/v1/restrooms/nearest?lat=53.9&lon=27.56&distanceMeters=300&limit=5") {
                    header("X-Client-Type", "telegram_bot")
                }
            assertEquals(HttpStatusCode.OK, nearestResponse.status)

            val metricsResponse = client.get("/metrics")
            assertEquals(HttpStatusCode.OK, metricsResponse.status)
            val body = metricsResponse.bodyAsText()

            assertTrue(body.contains("search_requests_total"))
            assertTrue(body.contains("client_type=\"telegram_bot\""))
            assertTrue(body.contains("radius_bucket=\"0_300\""))
            assertTrue(body.contains("search_results_total"))
            assertTrue(body.contains("result_bucket=\"1_2\""))
            assertTrue(body.contains("search_quality_total"))
            assertTrue(body.contains("quality_bucket=\"poor\""))
            assertNoForbiddenMetricLabels(body)
        }

    @Test
    fun `nearest endpoint invalid query params should still record request metric with safe radius bucket`() =
        testApplication {
            val restroomService = mockk<RestroomService>()
            val countryService = mockk<CountryService>(relaxed = true)
            val cityService = mockk<CityService>(relaxed = true)
            val importService = mockk<ImportService>(relaxed = true)
            val dslContext = mockk<DSLContext>(relaxed = true)
            val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            val backendSearchMetrics = BackendSearchMetrics(registry)

            application {
                install(Koin) {
                    modules(
                        module {
                            single<CountryService> { countryService }
                            single<CityService> { cityService }
                            single<RestroomService> { restroomService }
                            single<ImportService> { importService }
                            single<DSLContext> { dslContext }
                            single<PrometheusMeterRegistry> { registry }
                            single<BackendSearchMetrics> { backendSearchMetrics }
                            single<AnalyticsService> { mockk(relaxed = true) }
                            single<PublicAnalyticsService> { mockk(relaxed = true) }
                        },
                        module {
                            single { yayauheny.by.controller.CountryController(get()) }
                            single { yayauheny.by.controller.CityController(get()) }
                            single { yayauheny.by.controller.RestroomController(get(), get(), get()) }
                            single { yayauheny.by.controller.HealthController(get()) }
                            single { ImportController(get()) }
                            single { AnalyticsController(get()) }
                            single { PublicAnalyticsController(get()) }
                        }
                    )
                }
                install(ContentNegotiation) { json(testJson) }
                configureMetrics(registry)
                configureErrorHandling()
                configureRouting()
            }

            val nearestResponse = client.get("/api/v1/restrooms/nearest?lat=abc&lon=27.56&distanceMeters=abc&limit=5")
            assertEquals(HttpStatusCode.BadRequest, nearestResponse.status)

            val metricsResponse = client.get("/metrics")
            assertEquals(HttpStatusCode.OK, metricsResponse.status)
            val body = metricsResponse.bodyAsText()

            assertTrue(body.contains("search_requests_total"))
            assertTrue(body.contains("radius_bucket=\"unknown\""))
            assertTrue(body.contains("search_failures_total"))
            assertTrue(body.contains("reason=\"validation\""))
            assertNoForbiddenMetricLabels(body)
        }

    @Test
    fun `nearest endpoint should return restrooms even when analytics tracking fails`() =
        testApplication {
            val restroomService = mockk<RestroomService>()
            val countryService = mockk<CountryService>(relaxed = true)
            val cityService = mockk<CityService>(relaxed = true)
            val importService = mockk<ImportService>(relaxed = true)
            val dslContext = mockk<DSLContext>(relaxed = true)
            val registry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
            val backendSearchMetrics = BackendSearchMetrics(registry)
            val analyticsService = mockk<AnalyticsService>()

            coEvery {
                restroomService.findNearestRestrooms(53.9, 27.56, 5, 300)
            } returns
                listOf(
                    NearestRestroomSlimDto(
                        id = java.util.UUID.randomUUID(),
                        displayName = "Analytics fallback restroom",
                        distanceMeters = 250.0,
                        feeType = FeeType.FREE,
                        queryCoordinates = Coordinates(53.9, 27.56),
                        restroomCoordinates = Coordinates(53.9005, 27.5605)
                    )
                )
            coEvery { analyticsService.trackNearestRestroomsSearch(any(), any(), any(), any()) } throws RuntimeException("boom")

            application {
                install(Koin) {
                    modules(
                        module {
                            single<CountryService> { countryService }
                            single<CityService> { cityService }
                            single<RestroomService> { restroomService }
                            single<ImportService> { importService }
                            single<DSLContext> { dslContext }
                            single<PrometheusMeterRegistry> { registry }
                            single<BackendSearchMetrics> { backendSearchMetrics }
                            single<AnalyticsService> { analyticsService }
                            single<PublicAnalyticsService> { mockk(relaxed = true) }
                        },
                        module {
                            single { yayauheny.by.controller.CountryController(get()) }
                            single { yayauheny.by.controller.CityController(get()) }
                            single { yayauheny.by.controller.RestroomController(get(), get(), get()) }
                            single { yayauheny.by.controller.HealthController(get()) }
                            single { ImportController(get()) }
                            single { AnalyticsController(get()) }
                            single { PublicAnalyticsController(get()) }
                        }
                    )
                }
                install(ContentNegotiation) { json(testJson) }
                configureMetrics(registry)
                configureErrorHandling()
                configureRouting()
            }

            val nearestResponse =
                client.get("/api/v1/restrooms/nearest?lat=53.9&lon=27.56&distanceMeters=300&limit=5") {
                    header("X-Client-Type", "telegram_bot")
                }

            assertEquals(HttpStatusCode.OK, nearestResponse.status)
            assertTrue(nearestResponse.bodyAsText().contains("Analytics fallback restroom"))
        }

    private fun assertNoForbiddenMetricLabels(body: String) {
        val forbiddenFragments =
            listOf(
                "lat=",
                "lon=",
                "user_id=",
                "userId=",
                "chat_id=",
                "restroom_id="
            )

        forbiddenFragments.forEach { fragment ->
            assertTrue(
                !body.contains(fragment),
                "Custom metrics output must not contain forbidden fragment: $fragment"
            )
        }
    }
}
