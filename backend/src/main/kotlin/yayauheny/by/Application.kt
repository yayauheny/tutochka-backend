package yayauheny.by

import com.zaxxer.hikari.HikariDataSource
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import yayauheny.by.common.plugins.configureErrorHandling
import yayauheny.by.di.analyticsModule
import yayauheny.by.config.DatabaseConfig
import yayauheny.by.config.configureMetrics
import yayauheny.by.config.configureRequestLogging
import yayauheny.by.config.configureRouting
import yayauheny.by.config.runLiquibaseMigrations
import yayauheny.by.di.controllerModule
import yayauheny.by.di.databaseConfigModule
import yayauheny.by.di.importingModule
import yayauheny.by.di.serviceModule
import yayauheny.by.config.metricsModule
import yayauheny.by.util.InstantSerializer
import yayauheny.by.util.UUIDSerializer

fun main(args: Array<String>) =
    io.ktor.server.netty.EngineMain
        .main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(
            listOf(
                databaseConfigModule,
                serviceModule,
                analyticsModule,
                importingModule,
                controllerModule,
                metricsModule
            )
        )
    }

    val dataSource by inject<HikariDataSource>()
    val databaseConfig by inject<DatabaseConfig>()
    runLiquibaseMigrations(dataSource, databaseConfig.schema)

    val prometheusRegistry by inject<PrometheusMeterRegistry>()
    configureMetrics(prometheusRegistry)

    install(DefaultHeaders)
    configureRequestLogging()
    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                serializersModule =
                    SerializersModule {
                        contextual(UUID::class, UUIDSerializer)
                        contextual(Instant::class, InstantSerializer)
                    }
            }
        )
    }

    configureErrorHandling()
    configureRouting()
}
