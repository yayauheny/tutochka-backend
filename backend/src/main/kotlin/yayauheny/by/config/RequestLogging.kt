package yayauheny.by.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.path
import org.slf4j.Logger
import org.slf4j.event.Level

private val ignoredPaths =
    setOf(
        "/health/live",
        "/health/ready",
        "/metrics",
        "/actuator/health",
        "/actuator/prometheus"
    )

fun Application.configureRequestLogging(logger: Logger? = null) {
    install(CallLogging) {
        level = Level.INFO
        this.logger = logger ?: this@configureRequestLogging.environment.log
        filter { call ->
            !shouldIgnoreRequestPath(call.request.path())
        }
    }
}

fun shouldIgnoreRequestPath(path: String): Boolean = path.trimEnd('/') in ignoredPaths
