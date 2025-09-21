package yayauheny.by.service

import yayauheny.by.common.errors.ConflictException
import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.repository.CountryRepository
import java.util.UUID

class CountryService(
    private val countryRepository: CountryRepository
) {
    suspend fun getAllCountries(pagination: PaginationDto): PageResponseDto<CountryResponseDto> = countryRepository.findAll(pagination)

    suspend fun getCountryById(id: UUID): CountryResponseDto? = countryRepository.findById(id)

    suspend fun getCountryByCode(code: String): CountryResponseDto? = countryRepository.findByCode(code)

    suspend fun createCountry(createDto: CountryCreateDto): CountryResponseDto {
        if (countryRepository.existsByCode(createDto.code)) {
            throw ConflictException("Country with code '${createDto.code}' already exists")
        }

        val countryDto = createDto.toResponseDto(UUID.randomUUID())
        return countryRepository.save(countryDto)
    }

    suspend fun updateCountry(
        id: UUID,
        updateDto: CountryCreateDto
    ): CountryResponseDto? =
        countryRepository.findById(id)?.let { existing ->
            if (updateDto.code != existing.code && countryRepository.existsByCode(updateDto.code)) {
                throw ConflictException("Country with code '${updateDto.code}' already exists")
            }

            val updatedDto = updateDto.toResponseDto(id = existing.id)
            countryRepository.save(updatedDto)
        }

    suspend fun deleteCountry(id: UUID): Boolean = countryRepository.deleteById(id)

    suspend fun countryExists(code: String): Boolean = countryRepository.existsByCode(code)
}

private fun CountryCreateDto.toResponseDto(id: UUID) =
    CountryResponseDto(
        id = id,
        nameRu = nameRu,
        nameEn = nameEn,
        code = code
    )
