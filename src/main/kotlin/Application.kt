package yayauheny.by

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import yayauheny.by.config.configureRouting
import yayauheny.by.di.controllerModule
import yayauheny.by.di.databaseConfigModule
import yayauheny.by.di.serviceModule

fun main(args: Array<String>) = io.ktor.server.netty.EngineMain.main(args)

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(
            listOf(
                databaseConfigModule,
                serviceModule,
                controllerModule
            )
        )
    }
    install(DefaultHeaders)
    install(CallLogging)
    configureRouting()
}
