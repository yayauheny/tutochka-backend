package integration.base

import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jooq.DSLContext
import org.koin.ktor.plugin.Koin
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.config.configureRouting
import yayauheny.by.helpers.testJson

object KtorTestApplication {
    fun withApp(
        testDslContext: DSLContext,
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = testApplication {
        application {
            install(Koin) { modules(buildTestModules(testDslContext)) }
            install(ContentNegotiation) {
                json(testJson)
            }
            configureErrorHandling()
            configureRouting()
        }
        val client = createClient { expectSuccess = false }
        block(client)
    }
}
