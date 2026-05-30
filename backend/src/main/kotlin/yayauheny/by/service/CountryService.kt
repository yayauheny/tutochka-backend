package yayauheny.by.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import yayauheny.by.common.errors.ConflictException
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.entity.CountryEntity
import yayauheny.by.model.country.CountryFilter
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.repository.CountryJpaRepository
import yayauheny.by.util.toPageResponse
import java.util.UUID

fun CountryEntity.toDto() = CountryResponseDto(
    id = this.id!!,
    code = this.code,
    nameRu = this.nameRu,
    nameEn = this.nameEn
)


@Service
@Transactional
class CountryService(
    private val countryRepository: CountryJpaRepository
) {
    @Transactional(readOnly = true)
    fun getAllCountries(pagination: PaginationRequest): PageResponse<CountryResponseDto> {
        return getAllCountries(CountryFilter(), pagination)
    }

    @Transactional(readOnly = true)
    fun getAllCountries(filter: CountryFilter, pagination: PaginationRequest): PageResponse<CountryResponseDto> {
        val page = countryRepository.findAllByFilter(filter, pagination)
        val dtoPage = page.map { it.toDto() }
        return dtoPage.toPageResponse()
    }

    @Transactional(readOnly = true)
    fun getCountryById(id: UUID): CountryResponseDto? {
        return countryRepository.findById(id).orElse(null)?.toDto()
    }

    @Transactional(readOnly = true)
    fun getCountryByCode(code: String): CountryResponseDto? {
        return countryRepository.findByCode(code)?.toDto()
    }

    fun createCountry(createDto: CountryCreateDto): CountryResponseDto {
        val existing = countryRepository.findByCode(createDto.code)
        if (existing != null) {
            throw ConflictException("Страна с кодом '${createDto.code}' уже существует")
        }

        val entity = CountryEntity().apply {
            code = createDto.code
            nameRu = createDto.nameRu
            nameEn = createDto.nameEn
        }
        return countryRepository.save(entity).toDto()
    }

    fun updateCountry(id: UUID, updateDto: CountryUpdateDto): CountryResponseDto {
        val entity = countryRepository.findById(id).orElseThrow {
            IllegalArgumentException("Country not found")
        }
        
        updateDto.nameRu?.let { entity.nameRu = it }
        updateDto.nameEn?.let { entity.nameEn = it }
        
        return countryRepository.save(entity).toDto()
    }

    fun deleteCountry(id: UUID): Boolean {
        if (countryRepository.existsById(id)) {
            countryRepository.deleteById(id)
            return true
        }
        return false
    }

    @Transactional(readOnly = true)
    fun countryExists(code: String): Boolean {
        return countryRepository.findByCode(code) != null
    }
}
