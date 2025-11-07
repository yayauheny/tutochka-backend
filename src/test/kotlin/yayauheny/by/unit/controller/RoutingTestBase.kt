package yayauheny.by.unit.controller

import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.mockk
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import yayauheny.by.helpers.testJson
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.config.configureRouting
import yayauheny.by.controller.CityController
import yayauheny.by.controller.CountryController
import yayauheny.by.controller.RestroomController
import yayauheny.by.service.CityService
import yayauheny.by.service.CountryService
import yayauheny.by.service.RestroomService

abstract class RoutingTestBase {
    protected val countryService = mockk<CountryService>()
    protected val cityService = mockk<CityService>()
    protected val restroomService = mockk<RestroomService>()

    private fun buildMockModules(): List<Module> =
        listOf(
            module {
                single<CountryService> { countryService }
                single<CityService> { cityService }
                single<RestroomService> { restroomService }
            },
            module {
                single { CountryController(get()) }
                single { CityController(get()) }
                single { RestroomController(get()) }
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
