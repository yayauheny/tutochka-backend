package yayauheny.by.service

import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.util.toPageResponse

import yayauheny.by.entity.RestroomEntity
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository
import java.util.UUID

fun RestroomEntity.toDto() = RestroomResponseDto(
    id = this.id!!,
    cityId = this.city?.id,
    cityName = this.city?.nameRu,
    buildingId = this.building?.id,
    subwayStationId = this.subwayStation?.id,
    name = this.name,
    address = this.address,
    accessNote = this.accessNote,
    directionGuide = this.directionGuide,
    feeType = this.feeType,
    genderType = this.genderType,
    accessibilityType = this.accessibilityType,
    placeType = this.placeType,
    status = this.status,
    phones = emptyMap(),
    workTime = emptyMap(),
    inheritBuildingSchedule = this.inheritBuildingSchedule,
    amenities = emptyMap(),
    hasPhotos = this.hasPhotos,
    coordinates = yayauheny.by.model.dto.Coordinates(
        lat = this.coordinates?.y ?: 0.0,
        lon = this.coordinates?.x ?: 0.0
    ),
    externalMaps = emptyMap(),
    dataSource = this.dataSource,
    locationType = this.locationType,
    isHidden = this.isHidden,
    originProvider = this.originProvider,
    originId = this.originId,
    createdAt = this.createdAt!!,
    updatedAt = this.updatedAt!!
)

@Service
@Transactional
class RestroomService(
    private val restroomRepository: RestroomJpaRepository,
    private val cityRepository: CityJpaRepository,
    private val buildingRepository: BuildingJpaRepository,
    private val subwayStationRepository: SubwayStationJpaRepository
) {
    @Transactional(readOnly = true)
    fun getAllRestrooms(pagination: PaginationRequest): PageResponse<RestroomResponseDto> {
        val page = restroomRepository.findAll(PageRequest.of(pagination.page, pagination.size))
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun getRestroomById(id: UUID): RestroomResponseDto? {
        return restroomRepository.findById(id).orElse(null)?.toDto()
    }

    @Transactional(readOnly = true)
    fun getRestroomsByCity(cityId: UUID, pagination: PaginationRequest): PageResponse<RestroomResponseDto> {
        val page = restroomRepository.findByCityId(cityId, PageRequest.of(pagination.page, pagination.size))
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun findNearestRestrooms(
        latitude: Double,
        longitude: Double,
        limit: Int,
        distanceMeters: Int = 5000
    ): List<NearestRestroomSlimDto> {
        val entities = restroomRepository.findNearest(longitude, latitude, distanceMeters, limit)
        return entities.map {
            NearestRestroomSlimDto(
                id = it.id!!,
                displayName = it.name ?: it.address ?: "Unknown",
                distanceMeters = 0.0, // Needs distance calculation logic, simplified for now
                feeType = it.feeType,
                queryCoordinates = yayauheny.by.model.dto.Coordinates(lat = latitude, lon = longitude),
                restroomCoordinates = yayauheny.by.model.dto.Coordinates(
                    lat = it.coordinates?.y ?: 0.0,
                    lon = it.coordinates?.x ?: 0.0
                )
            )
        }
    }

    fun createRestroom(createDto: RestroomCreateDto): RestroomResponseDto {
        val entity = RestroomEntity().apply {
            city = createDto.cityId?.let { cityRepository.findById(it).orElse(null) }
            building = createDto.buildingId?.let { buildingRepository.findById(it).orElse(null) }
            name = createDto.name
            createDto.placeType?.let { placeType = it }
            address = createDto.address
            createDto.feeType?.let { feeType = it }
            createDto.genderType?.let { genderType = it }
            createDto.accessibilityType?.let { accessibilityType = it }
            createDto.status?.let { status = it }
            // Omitted complex mappings for brevity
        }
        return restroomRepository.save(entity).toDto()
    }

    fun updateRestroom(id: UUID, updateDto: RestroomUpdateDto): RestroomResponseDto {
        val entity = restroomRepository.findById(id).orElseThrow { IllegalArgumentException("Not found") }
        updateDto.name?.let { entity.name = it }
        updateDto.address?.let { entity.address = it }
        return restroomRepository.save(entity).toDto()
    }

    fun deleteRestroom(id: UUID): Boolean {
        if (restroomRepository.existsById(id)) {
            restroomRepository.deleteById(id)
            return true
        }
        return false
    }
}
