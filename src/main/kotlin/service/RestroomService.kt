package yayauheny.by.service

import yayauheny.by.model.RestroomCreateDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.repository.RestroomRepository
import java.time.Instant
import java.util.UUID

class RestroomService(private val restroomRepository: RestroomRepository) {
    
    suspend fun getAllRestrooms(): List<RestroomResponseDto> = 
        restroomRepository.findAll()
    
    suspend fun getRestroomById(id: UUID): RestroomResponseDto? = 
        restroomRepository.findById(id)
    
    suspend fun getRestroomsByCity(cityId: UUID): List<RestroomResponseDto> = 
        restroomRepository.findByCityId(cityId)
    
    suspend fun findNearestRestrooms(
        latitude: Double, 
        longitude: Double, 
        limit: Int = 5
    ): List<RestroomResponseDto> =
        restroomRepository.findNearestByLocation(latitude, longitude, limit)
    
    suspend fun createRestroom(createDto: RestroomCreateDto): RestroomResponseDto {
        val now = Instant.now()
        val restroomDto = createDto.toResponseDto(UUID.randomUUID(), now, now)
        return restroomRepository.save(restroomDto)
    }
    
    suspend fun updateRestroom(id: UUID, updateDto: RestroomCreateDto): RestroomResponseDto? =
        restroomRepository.findById(id)?.let { existing ->
            val updatedDto = updateDto.toResponseDto(
                id = existing.id,
                createdAt = existing.createdAt,
                updatedAt = Instant.now()
            )
            restroomRepository.save(updatedDto)
        }
    
    suspend fun deleteRestroom(id: UUID): Boolean = 
        restroomRepository.deleteById(id)
}

private fun RestroomCreateDto.toResponseDto(
    id: UUID,
    createdAt: Instant,
    updatedAt: Instant
) = RestroomResponseDto(
    id = id,
    cityId = cityId,
    code = code,
    description = description,
    name = name,
    workTime = workTime,
    feeType = feeType,
    accessibilityType = accessibilityType,
    lat = lat,
    lon = lon,
    dataSource = dataSource,
    amenities = amenities,
    createdAt = createdAt,
    updatedAt = updatedAt
)
