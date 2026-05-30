package yayauheny.by.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import yayauheny.by.common.errors.ApiError
import yayauheny.by.common.errors.ErrorResponse
import java.time.Instant

@RestController
class HealthController {

    @GetMapping("/health")
    fun healthCheck(): Map<String, String> {
        return mapOf("status" to "OK", "time" to Instant.now().toString())
    }

    @GetMapping("/api/v1/error-test")
    fun testError() {
        throw yayauheny.by.common.errors.NotFoundException("Test error")
    }
}
