package yayauheny.by.cache

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class RestroomCacheEventListener(
    private val restroomCacheService: RestroomCacheService
) {
    private val log = LoggerFactory.getLogger(RestroomCacheEventListener::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: RestroomCacheRestroomUpsertedEvent) {
        runCatching {
            restroomCacheService.refreshRestroom(event.restroom)
        }.onFailure {
            log.warn("Failed to refresh restroom cache after upsert: {}", event.restroom.id, it)
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handle(event: RestroomCacheRestroomDeletedEvent) {
        runCatching {
            restroomCacheService.removeRestroom(event.restroomId)
        }.onFailure {
            log.warn("Failed to remove restroom cache after delete: {}", event.restroomId, it)
        }
    }
}
