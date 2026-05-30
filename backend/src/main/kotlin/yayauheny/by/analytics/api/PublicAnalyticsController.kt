package yayauheny.by.analytics.api

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import yayauheny.by.analytics.model.PublicAnalyticsResponse
import yayauheny.by.analytics.service.PublicAnalyticsService

@RestController
@RequestMapping("/api/v1/public/analytics")
class PublicAnalyticsController(
    private val publicAnalyticsService: PublicAnalyticsService
) {
    @GetMapping
    fun getAnalytics(): PublicAnalyticsResponse {
        return publicAnalyticsService.getPublicAnalytics()
    }
}
