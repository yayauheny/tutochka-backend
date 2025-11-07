package yayauheny.by.service

import java.util.UUID
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.repository.impl.CountryRepositoryImpl
import yayauheny.by.service.validation.countryCreateValidator
import yayauheny.by.service.validation.validateOrThrow

class CountryService(
    private val countryRepository: CountryRepositoryImpl
) {
    suspend fun getAllCountries(pagination: PaginationRequest): PageResponse<CountryResponseDto> =
        countryRepository.findAll(pagination)

    suspend fun getCountryById(id: UUID): CountryResponseDto? =
        countryRepository.findById(id)

    suspend fun getCountryByCode(code: String): CountryResponseDto? =
        countryRepository.findSingle(
            listOf(FilterCriteria("code", FilterOperator.EQ, code))
        )

    suspend fun createCountry(createDto: CountryCreateDto): CountryResponseDto {
        createDto.validateOrThrow(countryCreateValidator)

        val existing = countryRepository.findSingle(
            listOf(FilterCriteria("code", FilterOperator.EQ, createDto.code))
        )
        if (existing != null) {
            throw ConflictException("Страна с кодом '${createDto.code}' уже существует")
        }

        return countryRepository.save(createDto)
    }

    suspend fun updateCountry(
        id: UUID,
        updateDto: CountryUpdateDto
    ): CountryResponseDto? =
        countryRepository.findById(id)?.let { existing ->
            if (updateDto.code != null && updateDto.code != existing.code) {
                val existingByCode = countryRepository.findSingle(
                    listOf(FilterCriteria("code", FilterOperator.EQ, updateDto.code))
                )
                if (existingByCode != null) {
                    throw ConflictException("Страна с кодом '${updateDto.code}' уже существует")
                }
            }

            countryRepository.update(id, updateDto)
        }

    suspend fun deleteCountry(id: UUID): Boolean =
        countryRepository.deleteById(id)

    suspend fun countryExists(code: String): Boolean =
        countryRepository.findSingle(
            listOf(FilterCriteria("code", FilterOperator.EQ, code))
        ) != null
}
