package yayauheny.by.cache

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "app.restroom-cache")
data class RestroomCacheProperties(
    val enabled: Boolean = true,
    val nearest: Nearest = Nearest(),
    val popularDetails: PopularDetails = PopularDetails()
) {
    data class Nearest(
        val enabled: Boolean = true,
        val keyPrefix: String = "restrooms:nearest",
        val startupLoadMode: StartupLoadMode = StartupLoadMode.FULL,
        val refreshMode: RefreshMode = RefreshMode.INCREMENTAL,
        val fallbackToDatabase: Boolean = true,
        val maxSearchRadiusMeters: Int = 20_000,
        val maxResultLimit: Int = 100,
        val candidateLimitMultiplier: Int = 10,
        val maxCandidateLimit: Int = 500,
        val loadPageSize: Int = 1_000,
        val redisPipelineBatchSize: Int = 500,
        val maxIncrementalChangeIds: Int = 1_000,
        val oldVersionTtl: Duration = Duration.ofMinutes(30),
        val rebuildLockTtl: Duration = Duration.ofMinutes(2),
        val minReadyItems: Int = 1
    )

    data class PopularDetails(
        val enabled: Boolean = true,
        val keyPrefix: String = "restrooms:popular",
        val algorithm: String = "lfu",
        val maxItems: Int = 100,
        val detailTtl: Duration = Duration.ofHours(24),
        val scoreTtl: Duration = Duration.ofDays(30),
        val refreshInterval: Duration = Duration.ofMinutes(5),
        val lfuDecayInterval: Duration = Duration.ofDays(1),
        val lfuDecayFactor: Double = 0.9,
        val loadPageSize: Int = 100,
        val redisPipelineBatchSize: Int = 100
    )

    enum class StartupLoadMode {
        FULL,
        LAZY
    }

    enum class RefreshMode {
        INCREMENTAL,
        FULL
    }
}
