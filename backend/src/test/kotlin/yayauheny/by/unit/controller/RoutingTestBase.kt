package yayauheny.by.unit.controller

import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import org.jooq.Record1
import org.jooq.SelectSelectStep
import org.jooq.impl.DSL
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import yayauheny.by.helpers.testJson
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.config.configureRouting
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.HealthController
import yayauheny.by.controller.ImportController
import yayauheny.by.controller.RestroomController
import yayauheny.by.metrics.BackendSearchMetrics
import org.jooq.DSLContext
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.repository.CityRepository
import yayauheny.by.service.RestroomService
import yayauheny.by.service.import.ImportService

abstract class RoutingTestBase {
    protected val countryService = mockk<CountryService>()
    protected val cityService = mockk<CityService>()
    protected val restroomService = mockk<RestroomService>()
    protected val importService = mockk<ImportService>(relaxed = true)
    private val dslContext =
        mockk<DSLContext>(relaxed = true) {
            val mockSelectStep = mockk<SelectSelectStep<Record1<Any>>>(relaxed = true)
            val mockRecord = mockk<Record1<Any>>(relaxed = true)
            every { select(DSL.field("1")) } returns mockSelectStep
            every { mockSelectStep.fetchOne() } returns mockRecord
        }

    private fun buildMockModules(): List<Module> =
        listOf(
            module {
                single<CountryService> { countryService }
                single<CityService> { cityService }
                single<RestroomService> { restroomService }
                single<ImportService> { importService }
                single<DSLContext> { dslContext }
                single<CityRepository> { mockk<CityRepository>(relaxed = true) }
            },
            module {
                single { CountryController(get()) }
                single { CityController(get()) }
                single { RestroomController(get(), mockk<BackendSearchMetrics>(relaxed = true)) }
                single { HealthController(get()) }
                single { ImportController(get(), get()) }
            }
        )

    fun withRoutingApp(block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit) =
        testApplication {
            application {
                install(Koin) { modules(buildMockModules()) }
                install(ContentNegotiation) { json(yayauheny.by.helpers.testJson) }
                configureErrorHandling()
                configureRouting()
            }
            val client = createClient { expectSuccess = false }
            block(client)
        }
}
