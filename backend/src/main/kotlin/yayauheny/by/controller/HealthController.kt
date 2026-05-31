package yayauheny.by.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import javax.sql.DataSource

@RestController
class HealthController(
    private val dataSource: DataSource
) {

    @GetMapping("/health", "/api/v1/health")
    fun healthCheck(): ResponseEntity<Map<String, String>> {
        return if (isDatabaseConnected()) {
            ResponseEntity.ok(
                mapOf(
                    "status" to "healthy",
                    "database" to "connected",
                    "time" to Instant.now().toString()
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                mapOf(
                    "status" to "unhealthy",
                    "database" to "disconnected",
                    "time" to Instant.now().toString()
                )
            )
        }
    }

    @GetMapping("/health/ready", "/api/v1/health/ready")
    fun readyCheck(): ResponseEntity<Map<String, String>> {
        return if (isDatabaseConnected()) {
            ResponseEntity.ok(
                mapOf(
                    "status" to "ready",
                    "database" to "connected",
                    "time" to Instant.now().toString()
                )
            )
        } else {
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                mapOf(
                    "status" to "not ready",
                    "database" to "disconnected",
                    "time" to Instant.now().toString()
                )
            )
        }
    }

    @GetMapping("/health/live", "/api/v1/health/live")
    fun liveCheck(): ResponseEntity<Map<String, String>> {
        return ResponseEntity.ok(
            mapOf(
                "status" to "alive",
                "service" to "TuTochka API",
                "time" to Instant.now().toString()
            )
        )
    }

    private fun isDatabaseConnected(): Boolean {
        return runCatching {
            dataSource.connection.use { connection ->
                connection.isValid(1)
            }
        }.getOrDefault(false)
    }
}
