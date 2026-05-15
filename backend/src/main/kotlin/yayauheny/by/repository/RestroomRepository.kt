package yayauheny.by.repository

import java.util.UUID
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto

interface RestroomRepository : BaseRepository<RestroomResponseDto, RestroomCreateDto, RestroomUpdateDto, UUID> {
    suspend fun findNearestByLocation(
        requestLat: Double,
        requestLon: Double,
        limit: Int,
        distanceMeters: Int? = ApiConstants.DEFAULT_MAX_DISTANCE_METERS
    ): List<NearestRestroomSlimDto>

    suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationRequest
    ): PageResponse<RestroomResponseDto>
}
