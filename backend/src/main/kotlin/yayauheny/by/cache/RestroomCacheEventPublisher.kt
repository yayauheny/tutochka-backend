package yayauheny.by.cache

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

interface RestroomCacheEventPublisher {
    fun restroomUpserted(restroom: RestroomResponseDto)

    fun restroomDeleted(restroomId: UUID)
}

object NoopRestroomCacheEventPublisher : RestroomCacheEventPublisher {
    override fun restroomUpserted(restroom: RestroomResponseDto) = Unit

    override fun restroomDeleted(restroomId: UUID) = Unit
}

@Component
@ConditionalOnProperty(prefix = "app.restroom-cache", name = ["enabled"], havingValue = "true")
class SpringRestroomCacheEventPublisher(
    private val applicationEventPublisher: ApplicationEventPublisher
) : RestroomCacheEventPublisher {
    override fun restroomUpserted(restroom: RestroomResponseDto) {
        applicationEventPublisher.publishEvent(RestroomCacheRestroomUpsertedEvent(restroom))
    }

    override fun restroomDeleted(restroomId: UUID) {
        applicationEventPublisher.publishEvent(RestroomCacheRestroomDeletedEvent(restroomId))
    }
}
