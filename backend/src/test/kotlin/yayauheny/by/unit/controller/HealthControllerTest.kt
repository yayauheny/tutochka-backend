package yayauheny.by.unit.controller

import io.ktor.client.HttpClient
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jooq.DSLContext
import org.jooq.Record1
import org.jooq.SelectSelectStep
import org.jooq.impl.DSL
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.config.configureRouting
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.ImportController
import yayauheny.by.controller.RestroomController
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testJson
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService
import yayauheny.by.repository.CityRepository
import yayauheny.by.service.import.ImportService

@DisplayName("HealthController Tests")
class HealthControllerTest {
    private val mockCtx = mockk<DSLContext>(relaxed = true)
    private val mockSelectStep = mockk<SelectSelectStep<Record1<Any>>>(relaxed = true)
    private val mockRecord = mockk<Record1<Any>>(relaxed = true)

    private fun buildMockModules(): List<Module> =
        listOf(
            module {
                single<DSLContext> { mockCtx }
                single<CountryService> { mockk<CountryService>(relaxed = true) }
                single<CityService> { mockk<CityService>(relaxed = true) }
                single<RestroomService> { mockk<RestroomService>(relaxed = true) }
                single<ImportService> { mockk<ImportService>(relaxed = true) }
                single<CityRepository> { mockk<CityRepository>(relaxed = true) }
            },
            module {
                single<CountryController> { CountryController(get()) }
                single<CityController> { CityController(get()) }
                single<RestroomController> { RestroomController(get()) }
                single<HealthController> { HealthController(get()) }
                single<ImportController> { ImportController(get(), get()) }
            }
        )

    private fun withRoutingApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        testApplication {
            application {
                install(Koin) { modules(buildMockModules()) }
                install(ContentNegotiation) { json(testJson) }
                configureErrorHandling()
                configureRouting()
            }
            val client = createClient { expectSuccess = false }
            block(client)
        }

    @Nested
    @DisplayName("healthRoutes Tests")
    inner class HealthRoutesTests {
        @Test
        @DisplayName("GIVEN healthy database WHEN GET /health THEN return 200 with healthy status")
        fun health_endpoint_with_healthy_db_returns_200() =
            runTest {
                every { mockCtx.select(DSL.field("1")) } returns mockSelectStep
                every { mockSelectStep.fetchOne() } returns mockRecord

                withRoutingApp { client ->
                    val response = client.testGet("/health")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"status\":\"healthy\""))
                    assertTrue(body.contains("\"database\":\"connected\""))
                }
            }

        @Test
        @DisplayName("GIVEN unhealthy database WHEN GET /health THEN return 503 with unhealthy status")
        fun health_endpoint_with_unhealthy_db_returns_503() =
            runTest {
                every { mockCtx.select(DSL.field("1")) } throws RuntimeException("Database connection failed")

                withRoutingApp { client ->
                    val response = client.testGet("/health")

                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"status\":\"unhealthy\""))
                    assertTrue(body.contains("\"database\":\"disconnected\""))
                }
            }

        @Test
        @DisplayName("GIVEN healthy database WHEN GET /health/ready THEN return 200 with ready status")
        fun health_ready_endpoint_with_healthy_db_returns_200() =
            runTest {
                every { mockCtx.select(DSL.field("1")) } returns mockSelectStep
                every { mockSelectStep.fetchOne() } returns mockRecord

                withRoutingApp { client ->
                    val response = client.testGet("/health/ready")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"status\":\"ready\""))
                    assertTrue(body.contains("\"database\":\"connected\""))
                }
            }

        @Test
        @DisplayName("GIVEN unhealthy database WHEN GET /health/ready THEN return 503 with not ready status")
        fun health_ready_endpoint_with_unhealthy_db_returns_503() =
            runTest {
                every { mockCtx.select(DSL.field("1")) } throws RuntimeException("Database connection failed")

                withRoutingApp { client ->
                    val response = client.testGet("/health/ready")

                    assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"status\":\"not ready\""))
                    assertTrue(body.contains("\"database\":\"disconnected\""))
                }
            }

        @Test
        @DisplayName("GIVEN any state WHEN GET /health/live THEN return 200 with alive status")
        fun health_live_endpoint_always_returns_200() =
            runTest {
                withRoutingApp { client ->
                    val response = client.testGet("/health/live")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"status\":\"alive\""))
                    assertTrue(body.contains("\"service\":\"TuTochka API\""))
                }
            }
    }
}
