package yayauheny.by.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit

class BackendMetrics(
    private val registry: MeterRegistry
) {
    // === Nearest restroom search ===

    private val nearestSearchSuccess: Counter =
        Counter
            .builder("restroom_nearest_search_total")
            .description(
                "Total number of nearest restroom geolocation searches that returned at least one result"
            ).tag("outcome", "found")
            .register(registry)

    private val nearestSearchEmpty: Counter =
        Counter
            .builder("restroom_nearest_search_total")
            .description(
                "Total number of nearest restroom geolocation searches that returned zero results within the given radius"
            ).tag("outcome", "not_found")
            .register(registry)

    private val nearestSearchError: Counter =
        Counter
            .builder("restroom_nearest_search_total")
            .description(
                "Total number of failed nearest restroom searches due to invalid coordinates or internal errors"
            ).tag("outcome", "error")
            .register(registry)

    private val nearestSearchDuration: Timer =
        Timer
            .builder("restroom_nearest_search_duration_seconds")
            .description(
                "Time spent executing a PostGIS nearest-neighbor query to find nearby restrooms"
            ).register(registry)

    // === City-based restroom requests ===

    fun recordCityRestroomRequest(cityName: String) {
        Counter
            .builder("restroom_by_city_requests_total")
            .description(
                "Total number of requests for restrooms in a specific city, grouped by city name"
            ).tag("city", cityName)
            .register(registry)
            .increment()
    }

    // === Record methods ===

    fun recordNearestSearchFound() = nearestSearchSuccess.increment()

    fun recordNearestSearchNotFound() = nearestSearchEmpty.increment()

    fun recordNearestSearchError() = nearestSearchError.increment()

    fun recordNearestSearchDuration(seconds: Double) = nearestSearchDuration.record(seconds, TimeUnit.SECONDS)
}
