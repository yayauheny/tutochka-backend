package yayauheny.by.repository

import org.springframework.data.domain.Page
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.city.CityFilter
import yayauheny.by.entity.CityEntity

interface CityFilterRepository {
    fun findAllByFilter(filter: CityFilter, pagination: PaginationRequest): Page<CityEntity>
}
