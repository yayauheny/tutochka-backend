package yayauheny.by.cache.detail

import yayauheny.by.model.restroom.RestroomResponseDto
import java.util.UUID

interface RestroomDetailCacheStore {
    fun get(restroomId: UUID): RestroomResponseDto?

    fun put(restroom: RestroomResponseDto)

    fun remove(restroomId: UUID)
}
