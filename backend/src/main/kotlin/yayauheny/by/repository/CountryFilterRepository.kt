package yayauheny.by.repository

import org.springframework.data.domain.Page
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.country.CountryFilter
import yayauheny.by.entity.CountryEntity

interface CountryFilterRepository {
    fun findAllByFilter(filter: CountryFilter, pagination: PaginationRequest): Page<CountryEntity>
}
