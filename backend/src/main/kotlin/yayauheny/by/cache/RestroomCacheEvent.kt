package yayauheny.by.cache

import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

data class RestroomCacheRestroomUpsertedEvent(
    val restroom: RestroomResponseDto
)

data class RestroomCacheRestroomDeletedEvent(
    val restroomId: UUID
)
