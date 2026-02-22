package yayauheny.by.controller

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory

class HealthController(
    private val ctx: DSLContext
) {
    private val logger = LoggerFactory.getLogger(HealthController::class.java)

    fun Route.healthRoutes() {
        route("/health") {
            get {
                val dbHealthy = checkDatabaseHealth()
                val status = if (dbHealthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
                call.respond(
                    status,
                    mapOf(
                        "status" to if (dbHealthy) "healthy" else "unhealthy",
                        "service" to "TuTochka API",
                        "database" to if (dbHealthy) "connected" else "disconnected"
                    )
                )
            }

            get("/ready") {
                val dbHealthy = checkDatabaseHealth()
                val status = if (dbHealthy) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable
                call.respond(
                    status,
                    mapOf(
                        "status" to if (dbHealthy) "ready" else "not ready",
                        "database" to if (dbHealthy) "connected" else "disconnected"
                    )
                )
            }

            get("/live") {
                call.respond(
                    mapOf(
                        "status" to "alive",
                        "service" to "TuTochka API"
                    )
                )
            }
        }
    }

    private suspend fun checkDatabaseHealth(): Boolean {
        return try {
            withContext(Dispatchers.IO) {
                ctx.select(DSL.field("1")).fetchOne() != null
            }
        } catch (e: Exception) {
            logger.warn("Database health check failed: ${e.message}", e)
            false
        }
    }
}
