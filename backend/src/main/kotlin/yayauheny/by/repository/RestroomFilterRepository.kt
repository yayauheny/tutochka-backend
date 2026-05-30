package yayauheny.by.repository

import org.springframework.data.domain.Page
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.entity.RestroomEntity

interface RestroomFilterRepository {
    fun findAllByFilter(filter: RestroomFilter, pagination: PaginationRequest): Page<RestroomEntity>
}
