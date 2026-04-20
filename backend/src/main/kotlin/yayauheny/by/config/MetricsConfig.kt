package yayauheny.by.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.koin.dsl.module
import yayauheny.by.metrics.BackendSearchMetrics

val metricsModule =
    module {
        single<PrometheusMeterRegistry> {
            PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        }
        single<BackendSearchMetrics> {
            BackendSearchMetrics(get<PrometheusMeterRegistry>())
        }
    }

fun Application.configureMetrics(registry: PrometheusMeterRegistry) {
    install(MicrometerMetrics) {
        this.registry = registry
        meterBinders = listOf() // JVM metrics auto-registered via registry
    }

    routing {
        get("/metrics") {
            call.respondText(registry.scrape())
        }
        get("/actuator/prometheus") {
            call.respondText(registry.scrape())
        }
    }
}
