package yayauheny.by.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.CityEntity
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.city.CityFilter
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityJpaRepository
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.util.toCoordinates
import yayauheny.by.util.toPageResponse
import yayauheny.by.util.toPoint
import java.util.UUID

fun CityEntity.toDto() = CityResponseDto(
    id = this.id!!,
    countryId = this.country!!.id!!,
    nameRu = this.nameRu,
    nameEn = this.nameEn,
    slug = this.slug,
    region = this.region,
    coordinates = this.coordinates.toCoordinates()
)

@Service
@Transactional
class CityService(
    private val cityRepository: CityJpaRepository,
    private val countryRepository: CountryJpaRepository
) {
    @Transactional(readOnly = true)
    fun getAllCities(pagination: PaginationRequest): PageResponse<CityResponseDto> {
        return getAllCities(CityFilter(), pagination)
    }

    @Transactional(readOnly = true)
    fun getAllCities(filter: CityFilter, pagination: PaginationRequest): PageResponse<CityResponseDto> {
        val page = cityRepository.findAllByFilter(filter, pagination)
        return page.map { it.toDto() }.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun getCityById(id: UUID): CityResponseDto? {
        return cityRepository.findById(id).orElse(null)?.toDto()
    }

    @Transactional(readOnly = true)
    fun getCitiesByCountry(countryId: UUID, pagination: PaginationRequest): PageResponse<CityResponseDto> {
        return getAllCities(CityFilter(countryId = countryId), pagination)
    }

    @Transactional(readOnly = true)
    fun searchCitiesByName(name: String, pagination: PaginationRequest): PageResponse<CityResponseDto> {
        return getAllCities(CityFilter(name = name), pagination)
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

        val slug = citySlug(country.code, createDto.nameEn)
        if (cityRepository.findBySlug(slug) != null) {
            throw ConflictException("Город со slug '${slug}' уже существует")
        }

        val entity = CityEntity().apply {
            this.country = country
            this.nameRu = createDto.nameRu
            this.nameEn = createDto.nameEn
            this.slug = slug
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
        val duplicateNameRu = cityRepository.findByCountryIdAndNameRu(updateDto.countryId, updateDto.nameRu)
        if (duplicateNameRu != null && duplicateNameRu.id != id) {
            throw ConflictException("Город с названием '${updateDto.nameRu}' уже существует в этой стране")
        }
        val duplicateNameEn = cityRepository.findByCountryIdAndNameEn(updateDto.countryId, updateDto.nameEn)
        if (duplicateNameEn != null && duplicateNameEn.id != id) {
            throw ConflictException("Город с названием '${updateDto.nameEn}' уже существует в этой стране")
        }

        val slug = citySlug(country.code, updateDto.nameEn)
        val duplicateSlug = cityRepository.findBySlug(slug)
        if (duplicateSlug != null && duplicateSlug.id != id) {
            throw ConflictException("Город со slug '${slug}' уже существует")
        }

        entity.country = country
        updateDto.nameRu?.let { entity.nameRu = it }
        updateDto.nameEn?.let { entity.nameEn = it }
        entity.slug = slug
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

    private fun citySlug(countryCode: String, cityNameEn: String): String {
        val normalizedCity = cityNameEn
            .trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "-")
            .trim('-')
        return "${countryCode.trim().lowercase()}-${normalizedCity}"
    }
}
