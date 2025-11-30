package yayauheny.by.repository

import java.util.UUID
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto

interface RestroomRepository : BaseRepository<RestroomResponseDto, RestroomCreateDto, RestroomUpdateDto, UUID> {
    suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int? = 5,
        distanceMeters: Int? = ApiConstants.DEFAULT_MAX_DISTANCE_METERS
    ): List<NearestRestroomResponseDto>

    suspend fun findByCityId(
        cityId: UUID,
        pagination: yayauheny.by.common.query.PaginationRequest
    ): yayauheny.by.common.query.PageResponse<RestroomResponseDto>
}
