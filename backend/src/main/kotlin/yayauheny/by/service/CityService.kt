package yayauheny.by.service

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.CityEntity
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.util.toPageRequest
import yayauheny.by.util.toPageResponse
import java.util.UUID

private val geometryFactory = GeometryFactory()

private fun Coordinates.toPoint(): Point =
    geometryFactory.createPoint(Coordinate(lon, lat))

fun CityEntity.toDto() = CityResponseDto(
    id = this.id!!,
    countryId = this.country!!.id!!,
    nameRu = this.nameRu,
    nameEn = this.nameEn,
    region = this.region,
    coordinates = yayauheny.by.model.dto.Coordinates(
        lat = this.coordinates?.y ?: 0.0,
        lon = this.coordinates?.x ?: 0.0
    )
)

@Service
@Transactional
class CityService(
    private val cityRepository: CityJpaRepository,
    private val countryRepository: CountryJpaRepository
) {
    @Transactional(readOnly = true)
    fun getAllCities(pagination: PaginationRequest): PageResponse<CityResponseDto> {
        val pageRequest = pagination.toPageRequest("nameRu")
        val page = cityRepository.findAll(pageRequest)
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun getCityById(id: UUID): CityResponseDto? {
        return cityRepository.findById(id).orElse(null)?.toDto()
    }

    @Transactional(readOnly = true)
    fun getCitiesByCountry(countryId: UUID, pagination: PaginationRequest): PageResponse<CityResponseDto> {
        val pageRequest = pagination.toPageRequest("nameRu")
        val page = cityRepository.findByCountryId(countryId, pageRequest)
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun searchCitiesByName(name: String, pagination: PaginationRequest): PageResponse<CityResponseDto> {
        val pageRequest = pagination.toPageRequest("nameRu")
        val page = cityRepository.findByNameRuContainingIgnoreCaseOrNameEnContainingIgnoreCase(name, name, pageRequest)
        return page.map { it.toDto() }.toPageResponse()
    }

    fun createCity(createDto: CityCreateDto): CityResponseDto {
        val country = countryRepository.findById(createDto.countryId).orElseThrow {
            IllegalArgumentException("Страна с ID '${createDto.countryId}' не найдена")
        }

        if (cityRepository.findByCountryIdAndNameRu(createDto.countryId, createDto.nameRu) != null) {
            throw ConflictException("Город с названием '${createDto.nameRu}' уже существует в этой стране")
        }

        if (cityRepository.findByCountryIdAndNameEn(createDto.countryId, createDto.nameEn) != null) {
            throw ConflictException("Город с названием '${createDto.nameEn}' уже существует в этой стране")
        }

        val entity = CityEntity().apply {
            this.country = country
            this.nameRu = createDto.nameRu
            this.nameEn = createDto.nameEn
            this.region = createDto.region
            this.coordinates = createDto.coordinates.toPoint()
        }

        return cityRepository.save(entity).toDto()
    }

    fun updateCity(id: UUID, updateDto: CityUpdateDto): CityResponseDto {
        val entity = cityRepository.findById(id).orElseThrow {
            IllegalArgumentException("City not found")
        }
        val country = countryRepository.findById(updateDto.countryId).orElseThrow {
            IllegalArgumentException("Country not found")
        }

        entity.country = country
        updateDto.nameRu?.let { entity.nameRu = it }
        updateDto.nameEn?.let { entity.nameEn = it }
        updateDto.region?.let { entity.region = it }
        entity.coordinates = updateDto.coordinates.toPoint()

        return cityRepository.save(entity).toDto()
    }

    fun deleteCity(id: UUID): Boolean {
        if (cityRepository.existsById(id)) {
            cityRepository.deleteById(id)
            return true
        }
        return false
    }
}
