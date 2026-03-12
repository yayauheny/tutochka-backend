package yayauheny.by.repository

import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest

interface BaseRepository<T, CreateDto, UpdateDto, ID> {
    suspend fun findAll(pagination: PaginationRequest): PageResponse<T>

    suspend fun findSingle(filters: List<FilterCriteria>): T?

    suspend fun findById(id: ID): T?

    suspend fun save(createDto: CreateDto): T

    suspend fun update(
        id: ID,
        updateDto: UpdateDto
    ): T

    suspend fun deleteById(id: ID): Boolean
}
