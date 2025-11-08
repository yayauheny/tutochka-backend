package yayauheny.by.service

import java.time.Instant
import java.util.UUID
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.model.enums.RestroomStatus

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
        limit: Int = 5
    ): List<NearestRestroomResponseDto> = restroomRepository.findNearestByLocation(latitude, longitude, limit)

    suspend fun createRestroom(createDto: RestroomCreateDto): RestroomResponseDto {
        return restroomRepository.save(createDto)
    }

    suspend fun updateRestroom(
        id: UUID,
        updateDto: RestroomUpdateDto
    ): RestroomResponseDto? = restroomRepository.update(id, updateDto)

    suspend fun deleteRestroom(id: UUID): Boolean = restroomRepository.deleteById(id)
}

private fun RestroomCreateDto.toResponseDto(
    id: UUID,
    createdAt: Instant,
    updatedAt: Instant,
    distanceMeters: Int? = null
) = RestroomResponseDto(
    id = id,
    cityId = cityId,
    name = name,
    description = description,
    address = address,
    phones = phones,
    workTime = workTime,
    feeType = feeType,
    accessibilityType = accessibilityType,
    lat = lat,
    lon = lon,
    dataSource = dataSource,
    status = RestroomStatus.ACTIVE,
    amenities = amenities,
    parentPlaceName = parentPlaceName,
    parentPlaceType = parentPlaceType,
    inheritParentSchedule = inheritParentSchedule,
    createdAt = createdAt,
    updatedAt = updatedAt,
    distanceMeters = distanceMeters
)
