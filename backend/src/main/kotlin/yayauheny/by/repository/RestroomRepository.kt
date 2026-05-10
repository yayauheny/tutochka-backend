package yayauheny.by.repository

import java.util.UUID
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.enums.ImportProvider
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

    /**
     * Находит туалет по origin_provider и origin_id.
     * Используется для upsert логики при импорте.
     * @param originProvider провайдер (например, ImportProvider.TWO_GIS)
     * @param originId ID от провайдера
     * @return найденный туалет или null
     */
    suspend fun findByOrigin(
        originProvider: ImportProvider,
        originId: String
    ): RestroomResponseDto?
}
