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

    /**
     * Находит туалет по внешнему ID из external_maps JSONB поля.
     * @param provider провайдер (например, "2gis")
     * @param externalId внешний ID от провайдера
     * @return найденный туалет или null
     */
    suspend fun findByExternalMap(
        provider: String,
        externalId: String
    ): RestroomResponseDto?
}
