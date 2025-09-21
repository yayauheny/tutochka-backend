package integration.base

import io.ktor.client.HttpClient
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.koin.ktor.plugin.Koin
import support.helpers.testJson
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.config.configureRouting

object KtorTestApplication {
    fun withApp(
        testDatabase: Database,
        block: suspend ApplicationTestBuilder.(client: HttpClient) -> Unit
    ) = testApplication {
        application {
            install(Koin) { modules(buildTestModules(testDatabase)) }
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
