package yayauheny.by.cache

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.event.EventListener
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import yayauheny.by.cache.nearest.NearestRestroomCacheItem
import yayauheny.by.model.enums.FeeType
import yayauheny.by.repository.RestroomJpaRepository

@Component
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class RestroomCacheStartupLoader(
    private val properties: RestroomCacheProperties,
    private val restroomRepository: RestroomJpaRepository,
    private val restroomCacheService: RestroomCacheService
) {
    private val log = LoggerFactory.getLogger(RestroomCacheStartupLoader::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun warmUp() {
        if (!properties.nearest.enabled || properties.nearest.startupLoadMode != RestroomCacheProperties.StartupLoadMode.FULL) {
            return
        }

        val rebuildVersion = restroomCacheService.beginRebuild() ?: return
        runCatching {
            val pageSize = properties.nearest.loadPageSize.coerceAtLeast(1)
            var page = 0
            var totalItems = 0L

            while (true) {
                val result = restroomRepository.findActiveNearestCacheRows(PageRequest.of(page, pageSize))
                val items = result.content.map { row ->
                    NearestRestroomCacheItem(
                        id = row.id,
                        lat = row.lat,
                        lon = row.lon,
                        displayName = row.displayName,
                        feeType = runCatching { FeeType.valueOf(row.feeType.uppercase()) }.getOrDefault(FeeType.UNKNOWN)
                    )
                }
                if (items.isNotEmpty()) {
                    restroomCacheService.appendRebuild(rebuildVersion, items)
                    totalItems += items.size
                }
                if (!result.hasNext()) {
                    break
                }
                page += 1
            }

            restroomCacheService.finishRebuild(rebuildVersion, totalItems)
        }.onFailure {
            restroomCacheService.abortRebuild(rebuildVersion)
            log.warn("Failed to warm up restroom nearest cache", it)
        }
    }
}
