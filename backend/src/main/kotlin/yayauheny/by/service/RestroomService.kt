package yayauheny.by.service

import by.yayauheny.shared.dto.NearestRestroomSlimDto
import java.util.UUID
import yayauheny.by.common.query.PageResponse
import yayauheny.by.config.ApiConstants
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomRepository

class RestroomService(
    private val restroomRepository: RestroomRepository
) {
    suspend fun getAllRestrooms(pagination: PaginationRequest): PageResponse<RestroomResponseDto> = restroomRepository.findAll(pagination)

    suspend fun getRestroomById(id: UUID): RestroomResponseDto? = restroomRepository.findById(id)

    suspend fun getRestroomsByCity(
        cityId: UUID,
        pagination: PaginationRequest
    ): PageResponse<RestroomResponseDto> = restroomRepository.findByCityId(cityId, pagination)

    suspend fun findNearestRestrooms(
        latitude: Double,
        longitude: Double,
        limit: Int = 5,
        distanceMeters: Int? = ApiConstants.DEFAULT_MAX_DISTANCE_METERS
    ): List<NearestRestroomSlimDto> = restroomRepository.findNearestByLocation(latitude, longitude, limit, distanceMeters)

    suspend fun createRestroom(createDto: RestroomCreateDto): RestroomResponseDto {
        return restroomRepository.save(createDto)
    }

    suspend fun updateRestroom(
        id: UUID,
        updateDto: RestroomUpdateDto
    ): RestroomResponseDto = restroomRepository.update(id, updateDto)

    suspend fun deleteRestroom(id: UUID): Boolean = restroomRepository.deleteById(id)
}
