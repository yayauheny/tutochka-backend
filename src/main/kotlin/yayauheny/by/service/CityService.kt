package yayauheny.by.service

import java.util.UUID
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import yayauheny.by.service.validation.validateCityOnCreate
import yayauheny.by.service.validation.validateOrThrow
import yayauheny.by.service.validation.validateRegion

class CityService(
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository
) {
    suspend fun getAllCities(pagination: PaginationRequest): PageResponse<CityResponseDto> = cityRepository.findAll(pagination)

    suspend fun getCityById(id: UUID): CityResponseDto? = cityRepository.findById(id)

    suspend fun getCitiesByCountry(
        countryId: UUID,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> = cityRepository.findByCountryId(countryId, pagination)

    suspend fun searchCitiesByName(
        name: String,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> = cityRepository.findByName(name, pagination)

    suspend fun createCity(createDto: CityCreateDto): CityResponseDto {
        createDto.validateOrThrow(validateCityOnCreate)

        val regionErrors = validateRegion(createDto.region)
        if (regionErrors.isNotEmpty()) {
            throw yayauheny.by.common.errors
                .ValidationException(errors = regionErrors)
        }

        countryRepository.findById(createDto.countryId)
            ?: throw IllegalArgumentException("Страна с ID '${createDto.countryId}' не найдена")

        val existingByRu =
            cityRepository.findSingle(
                listOf(
                    FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
                    FilterCriteria("nameRu", FilterOperator.EQ, createDto.nameRu)
                )
            )
        if (existingByRu != null) {
            throw yayauheny.by.common.errors
                .ConflictException("Город с названием '${createDto.nameRu}' уже существует в этой стране")
        }

        val existingByEn =
            cityRepository.findSingle(
                listOf(
                    FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
                    FilterCriteria("nameEn", FilterOperator.EQ, createDto.nameEn)
                )
            )
        if (existingByEn != null) {
            throw yayauheny.by.common.errors
                .ConflictException("Город с названием '${createDto.nameEn}' уже существует в этой стране")
        }

        return cityRepository.save(createDto)
    }

    suspend fun updateCity(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto = cityRepository.update(id, updateDto)

    suspend fun deleteCity(id: UUID): Boolean = cityRepository.deleteById(id)
}
