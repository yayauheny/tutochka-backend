package yayauheny.by.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@RestController
class HealthController {

    @GetMapping("/health", "/api/v1/health")
    fun healthCheck(): Map<String, String> {
        return mapOf("status" to "OK", "time" to Instant.now().toString())
    }

    @GetMapping("/health/ready", "/api/v1/health/ready")
    fun readyCheck(): Map<String, String> {
        return mapOf("status" to "ready", "time" to Instant.now().toString())
    }

    @GetMapping("/health/live", "/api/v1/health/live")
    fun liveCheck(): Map<String, String> {
        return mapOf("status" to "alive", "time" to Instant.now().toString())
    }
}
