package yayauheny.by.service

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.BuildingEntity
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.RestroomEntity
import yayauheny.by.entity.SubwayLineEntity
import yayauheny.by.entity.SubwayStationEntity
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomFilter
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.repository.BuildingJpaRepository
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.RestroomJpaRepository
import yayauheny.by.repository.SubwayStationJpaRepository
import yayauheny.by.util.toPageResponse
import java.util.UUID
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val geometryFactory = GeometryFactory()

private fun Coordinates.toPoint(): Point = geometryFactory.createPoint(Coordinate(lon, lat))

private fun Point?.toCoordinates(): Coordinates = Coordinates(lat = this?.y ?: 0.0, lon = this?.x ?: 0.0)

private fun Map<String, Any>?.toBooleanMap(): Map<String, Boolean>? =
    this?.entries?.associate { (key, value) ->
        key to (value as? Boolean ?: value.toString().toBooleanStrictOrNull() ?: false)
    }

private fun Map<String, Any>?.toStringMap(): Map<String, String>? =
    this?.entries?.associate { (key, value) -> key to value.toString() }

private fun List<String>?.toPhoneMap(): Map<String, Any>? =
    this?.associateWith { it }

private fun Map<String, Any>?.toPhoneList(): List<String>? =
    this?.values?.map { it.toString() }?.filter { it.isNotBlank() }

private fun BuildingEntity.toDto() = BuildingResponseDto(
    id = requireNotNull(id),
    cityId = requireNotNull(city?.id),
    name = name,
    address = address,
    buildingType = runCatching {
        buildingType?.let { yayauheny.by.model.enums.PlaceType.valueOf(it.uppercase()) }
    }.getOrNull(),
    workTime = workTime,
    coordinates = coordinates.toCoordinates(),
    externalIds = externalIds?.mapValues { it.value },
    isDeleted = isDeleted,
    createdAt = requireNotNull(createdAt),
    updatedAt = requireNotNull(updatedAt)
)

private fun SubwayLineEntity.toDto() = SubwayLineResponseDto(
    id = requireNotNull(id),
    cityId = requireNotNull(city?.id),
    nameRu = nameRu,
    nameEn = nameEn,
    shortCode = shortCode,
    hexColor = hexColor,
    isDeleted = isDeleted,
    createdAt = requireNotNull(createdAt)
)

private fun SubwayStationEntity.toDto() = SubwayStationResponseDto(
    id = requireNotNull(id),
    subwayLineId = subwayLine?.id,
    nameRu = nameRu,
    nameEn = nameEn,
    isTransfer = isTransfer,
    coordinates = coordinates.toCoordinates(),
    isDeleted = isDeleted,
    createdAt = requireNotNull(createdAt),
    line = subwayLine?.toDto()
)

private fun RestroomEntity.toDto(distanceMeters: Int? = null) = RestroomResponseDto(
    id = requireNotNull(id),
    cityId = city?.id,
    cityName = city?.nameRu,
    buildingId = building?.id,
    subwayStationId = subwayStation?.id,
    name = name,
    address = address,
    phones = phones.toPhoneMap(),
    workTime = workTime,
    feeType = feeType,
    genderType = genderType,
    accessibilityType = accessibilityType,
    placeType = placeType,
    coordinates = coordinates.toCoordinates(),
    dataSource = dataSource,
    status = status,
    amenities = amenities.toBooleanMap(),
    externalMaps = externalMaps?.mapValues { it.value },
    accessNote = accessNote,
    directionGuide = directionGuide,
    inheritBuildingSchedule = inheritBuildingSchedule,
    hasPhotos = hasPhotos,
    locationType = locationType,
    originProvider = originProvider,
    originId = originId,
    isHidden = isHidden,
    createdAt = requireNotNull(createdAt),
    updatedAt = requireNotNull(updatedAt),
    distanceMeters = distanceMeters,
    building = building?.toDto(),
    subwayStation = subwayStation?.toDto()
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
        return getAllRestrooms(RestroomFilter(), pagination)
    }

    @Transactional(readOnly = true)
    fun getAllRestrooms(filter: RestroomFilter, pagination: PaginationRequest): PageResponse<RestroomResponseDto> {
        val page = restroomRepository.findAllByFilter(filter, pagination)
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun getRestroomById(id: UUID): RestroomResponseDto? {
        return restroomRepository.findById(id).orElse(null)?.toDto()
    }

    @Transactional(readOnly = true)
    fun getRestroomsByCity(cityId: UUID, pagination: PaginationRequest): PageResponse<RestroomResponseDto> {
        return getAllRestrooms(RestroomFilter(cityId = cityId), pagination)
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
            val restroomCoordinates = it.coordinates.toCoordinates()
            NearestRestroomSlimDto(
                id = requireNotNull(it.id),
                displayName = it.name?.takeIf { name -> name.isNotBlank() } ?: it.address?.takeIf { address -> address.isNotBlank() } ?: "Unknown",
                distanceMeters = haversineMeters(latitude, longitude, restroomCoordinates.lat, restroomCoordinates.lon),
                feeType = it.feeType,
                queryCoordinates = Coordinates(lat = latitude, lon = longitude),
                restroomCoordinates = restroomCoordinates
            )
        }
    }

    fun createRestroom(createDto: RestroomCreateDto): RestroomResponseDto {
        val entity = RestroomEntity().apply {
            city = createDto.cityId?.let { resolveCity(it) }
            building = createDto.buildingId?.let { resolveBuilding(it) }
            subwayStation = createDto.subwayStationId?.let { resolveSubwayStation(it) }
            name = createDto.name
            placeType = createDto.placeType
            address = createDto.address
            phones = createDto.phones.toPhoneList()
            workTime = createDto.workTime
            feeType = createDto.feeType ?: feeType
            genderType = createDto.genderType ?: genderType
            accessibilityType = createDto.accessibilityType
            status = createDto.status
            amenities = createDto.amenities.toBooleanMap()
            externalMaps = createDto.externalMaps.toStringMap()
            accessNote = createDto.accessNote
            directionGuide = createDto.directionGuide
            inheritBuildingSchedule = createDto.inheritBuildingSchedule
            hasPhotos = createDto.hasPhotos
            coordinates = createDto.coordinates.toPoint()
            dataSource = createDto.dataSource
            locationType = createDto.locationType
            originProvider = createDto.originProvider
            originId = createDto.originId
            isHidden = createDto.isHidden
        }
        return restroomRepository.saveAndFlush(entity).toDto()
    }

    fun updateRestroom(id: UUID, updateDto: RestroomUpdateDto): RestroomResponseDto {
        val entity = restroomRepository.findById(id).orElseThrow { IllegalArgumentException("Not found") }
        updateDto.cityId?.let { entity.city = resolveCity(it) }
        updateDto.buildingId?.let { entity.building = resolveBuilding(it) }
        updateDto.subwayStationId?.let { entity.subwayStation = resolveSubwayStation(it) }
        updateDto.name?.let { entity.name = it }
        updateDto.placeType?.let { entity.placeType = it }
        updateDto.address?.let { entity.address = it }
        updateDto.phones?.let { entity.phones = it.toPhoneList() }
        updateDto.workTime?.let { entity.workTime = it }
        updateDto.feeType?.let { entity.feeType = it }
        updateDto.genderType?.let { entity.genderType = it }
        updateDto.accessibilityType?.let { entity.accessibilityType = it }
        updateDto.status?.let { entity.status = it }
        updateDto.amenities?.let { entity.amenities = it.toBooleanMap() }
        updateDto.externalMaps?.let { entity.externalMaps = it.toStringMap() }
        updateDto.accessNote?.let { entity.accessNote = it }
        updateDto.directionGuide?.let { entity.directionGuide = it }
        updateDto.inheritBuildingSchedule?.let { entity.inheritBuildingSchedule = it }
        updateDto.hasPhotos?.let { entity.hasPhotos = it }
        updateDto.locationType?.let { entity.locationType = it }
        updateDto.originProvider?.let { entity.originProvider = it }
        updateDto.originId?.let { entity.originId = it }
        updateDto.isHidden?.let { entity.isHidden = it }
        updateDto.coordinates?.let { entity.coordinates = it.toPoint() }
        return restroomRepository.saveAndFlush(entity).toDto()
    }

    fun deleteRestroom(id: UUID): Boolean {
        if (restroomRepository.existsById(id)) {
            restroomRepository.deleteById(id)
            return true
        }
        return false
    }

    private fun resolveCity(cityId: UUID): CityEntity =
        cityRepository.findById(cityId).orElseThrow { IllegalArgumentException("City not found") }

    private fun resolveBuilding(buildingId: UUID): BuildingEntity =
        buildingRepository.findById(buildingId).orElseThrow { IllegalArgumentException("Building not found") }

    private fun resolveSubwayStation(subwayStationId: UUID): SubwayStationEntity =
        subwayStationRepository.findById(subwayStationId).orElseThrow { IllegalArgumentException("Subway station not found") }

    private fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
            sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadius * c
    }
}
