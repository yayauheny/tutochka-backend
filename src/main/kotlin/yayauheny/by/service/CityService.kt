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
import yayauheny.by.service.validation.cityCreateValidator
import yayauheny.by.service.validation.validateOrThrow

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
        createDto.validateOrThrow(cityCreateValidator)

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
            throw IllegalArgumentException("Город с названием '${createDto.nameRu}' уже существует в этой стране")
        }

        val existingByEn =
            cityRepository.findSingle(
                listOf(
                    FilterCriteria("countryId", FilterOperator.EQ, createDto.countryId.toString()),
                    FilterCriteria("nameEn", FilterOperator.EQ, createDto.nameEn)
                )
            )
        if (existingByEn != null) {
            throw IllegalArgumentException("Город с названием '${createDto.nameEn}' уже существует в этой стране")
        }

        return cityRepository.save(createDto)
    }

    suspend fun updateCity(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto? =
        cityRepository.findById(id)?.let { existing ->
            countryRepository.findById(updateDto.countryId)
                ?: throw IllegalArgumentException("Страна с ID '${updateDto.countryId}' не найдена")

            val countryIdToCheck = updateDto.countryId ?: existing.countryId

            if (updateDto.nameRu != existing.nameRu) {
                val existingByRu =
                    cityRepository.findSingle(
                        listOf(
                            FilterCriteria("countryId", FilterOperator.EQ, countryIdToCheck.toString()),
                            FilterCriteria("nameRu", FilterOperator.EQ, updateDto.nameRu)
                        )
                    )
                if (existingByRu != null) {
                    throw IllegalArgumentException("Город с русским названием '${updateDto.nameRu}' уже существует в этой стране")
                }
            }

            if (updateDto.nameEn != existing.nameEn) {
                val existingByEn =
                    cityRepository.findSingle(
                        listOf(
                            FilterCriteria("countryId", FilterOperator.EQ, countryIdToCheck.toString()),
                            FilterCriteria("nameEn", FilterOperator.EQ, updateDto.nameEn)
                        )
                    )
                if (existingByEn != null) {
                    throw IllegalArgumentException("Город с английским названием '${updateDto.nameEn}' уже существует в этой стране")
                }
            }

            cityRepository.update(id, updateDto)
        }

    suspend fun deleteCity(id: UUID): Boolean = cityRepository.deleteById(id)
}
