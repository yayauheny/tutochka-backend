package yayauheny.by.repository

import java.util.UUID
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.GeoPoint
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.model.RestroomResponseDto
import yayauheny.by.table.RestroomsTable

interface RestroomRepository {
    suspend fun findAll(pagination: PaginationDto): PageResponseDto<RestroomResponseDto>

    suspend fun findById(id: UUID): RestroomResponseDto?

    suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationDto
    ): PageResponseDto<RestroomResponseDto>

    suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int = 5
    ): List<RestroomResponseDto>

    suspend fun save(restroom: RestroomResponseDto): RestroomResponseDto

    suspend fun deleteById(id: UUID): Boolean
}

class RestroomRepositoryImpl : RestroomRepository {
    override suspend fun findAll(pagination: PaginationDto): PageResponseDto<RestroomResponseDto> =
        newSuspendedTransaction {
            val totalCount = RestroomEntity.all().count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size

            val content =
                RestroomEntity
                    .all()
                    .limit(pagination.size)
                    .offset(offset.toLong())
                    .map { it.toResponseDto() }

            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }

    override suspend fun findById(id: UUID): RestroomResponseDto? =
        newSuspendedTransaction {
            RestroomEntity.findById(id)?.toResponseDto()
        }

    override suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationDto
    ): PageResponseDto<RestroomResponseDto> =
        newSuspendedTransaction {
            val query = RestroomEntity.find { RestroomsTable.cityId eq cityId }
            val totalCount = query.count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size

            val content =
                query
                    .limit(pagination.size)
                    .offset(offset.toLong())
                    .map { it.toResponseDto() }

            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }

    override suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int
    ): List<RestroomResponseDto> =
        newSuspendedTransaction {
            // TODO: refactor
            val allRestrooms =
                RestroomEntity
                    .all()
                    .limit(limit)
                    .map { it.toResponseDto() }
                    .sortedBy { calculateDistance(latitude, longitude, it.lat, it.lon) }
                    .take(limit)

            allRestrooms
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

    private fun createNewEntity(dto: RestroomResponseDto) =
        RestroomEntity.new {
            updateFromDto(dto)
        }

    private fun RestroomEntity.updateFromDto(dto: RestroomResponseDto) {
        name = dto.name
        description = dto.description
        address = dto.address
        phones = dto.phones
        workTime = dto.workTime
        feeType = dto.feeType
        accessibilityType = dto.accessibilityType
        coordinates = GeoPoint(dto.lon, dto.lat)
        dataSource = dto.dataSource
        status = dto.status
        amenities = dto.amenities
    }

    private fun RestroomEntity.toResponseDto() =
        RestroomResponseDto(
            id = id.value,
            cityId = city?.id?.value,
            name = name,
            description = description,
            address = address,
            phones = phones,
            workTime = workTime,
            feeType = feeType,
            accessibilityType = accessibilityType,
            lat = coordinates.latitude,
            lon = coordinates.longitude,
            dataSource = dataSource,
            status = status,
            amenities = amenities,
            createdAt = createdAt,
            updatedAt = updatedAt
        )

    private fun calculateDistance(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        val earthRadius = 6_371_000.0 // Earth's radius in meters
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        return earthRadius * c
    }
}
