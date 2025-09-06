package yayauheny.by.repository

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.enums.DataSourceType
import yayauheny.by.table.RestroomsTable
import yayauheny.by.util.toJsonObject
import java.util.UUID

interface RestroomRepository {
    suspend fun findAll(): List<RestroomResponseDto>
    suspend fun findById(id: UUID): RestroomResponseDto?
    suspend fun findByCityId(cityId: UUID): List<RestroomResponseDto>
    suspend fun findNearestByLocation(latitude: Double, longitude: Double, limit: Int = 5): List<RestroomResponseDto>
    suspend fun save(restroom: RestroomResponseDto): RestroomResponseDto
    suspend fun deleteById(id: UUID): Boolean
}

class RestroomRepositoryImpl : RestroomRepository {
    
    override suspend fun findAll(): List<RestroomResponseDto> = 
        newSuspendedTransaction { 
            RestroomEntity.all().map { it.toResponseDto() } 
        }
    
    override suspend fun findById(id: UUID): RestroomResponseDto? = 
        newSuspendedTransaction { 
            RestroomEntity.findById(id)?.toResponseDto() 
        }
    
    override suspend fun findByCityId(cityId: UUID): List<RestroomResponseDto> = 
        newSuspendedTransaction { 
            RestroomEntity.find { RestroomsTable.cityId eq cityId }
                .map { it.toResponseDto() }
        }
    
    override suspend fun findNearestByLocation(latitude: Double, longitude: Double, limit: Int): List<RestroomResponseDto> = 
        newSuspendedTransaction { 
            RestroomEntity.all()
                .map { it.toResponseDto() }
                .sortedBy { calculateDistance(latitude, longitude, it.lat, it.lon) }
                .take(limit)
        }
    
    override suspend fun save(restroom: RestroomResponseDto): RestroomResponseDto = 
        newSuspendedTransaction {
            val entity = RestroomEntity.findById(restroom.id) ?: createNewEntity(restroom)
            entity.updateFromDto(restroom)
            entity.toResponseDto()
        }
    
    override suspend fun deleteById(id: UUID): Boolean = 
        newSuspendedTransaction { 
            RestroomEntity.findById(id)?.delete() != null 
        }
    
    private fun createNewEntity(dto: RestroomResponseDto) = RestroomEntity.new {
        updateFromDto(dto)
    }
    
    private fun RestroomEntity.updateFromDto(dto: RestroomResponseDto) {
        code = dto.code
        description = dto.description
        name = dto.name
        workTime = dto.workTime
        feeType = dto.feeType
        accessibilityType = dto.accessibilityType
        dataSource = DataSourceType.valueOf(dto.dataSource)
        amenities = dto.amenities.toMap()
    }
    
    private fun RestroomEntity.toResponseDto() = RestroomResponseDto(
        id = id.value,
        cityId = city?.id?.value,
        code = code,
        description = description,
        name = name,
        workTime = workTime,
        feeType = feeType,
        accessibilityType = accessibilityType,
        lat = coordinates.latitude,
        lon = coordinates.longitude,
        dataSource = dataSource.name,
        amenities = amenities.toJsonObject(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
}
