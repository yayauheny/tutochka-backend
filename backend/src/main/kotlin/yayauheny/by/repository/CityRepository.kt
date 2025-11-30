package yayauheny.by.repository

import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import java.util.UUID

interface CityRepository : BaseRepository<CityResponseDto, CityCreateDto, CityUpdateDto, UUID> {
    suspend fun findByCountryId(
        countryId: UUID,
        pagination: yayauheny.by.common.query.PaginationRequest
    ): yayauheny.by.common.query.PageResponse<CityResponseDto>

    suspend fun findByName(
        name: String,
        pagination: yayauheny.by.common.query.PaginationRequest
    ): yayauheny.by.common.query.PageResponse<CityResponseDto>
}
