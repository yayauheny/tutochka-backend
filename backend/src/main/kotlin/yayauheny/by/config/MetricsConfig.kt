package yayauheny.by.config

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import org.koin.dsl.module

val metricsModule =
    module {
        single<PrometheusMeterRegistry> {
            PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
        }
        single<BackendMetrics> {
            BackendMetrics(get<PrometheusMeterRegistry>())
        }
    }

fun Application.configureMetrics(registry: PrometheusMeterRegistry) {
    install(MicrometerMetrics) {
        registry = this@configureMetrics.registry
        meterBinders = listOf() // JVM metrics auto-registered via registry
    }

    routing {
        get("/actuator/prometheus") {
            call.respondText(registry.scrape())
        }
    }
}
