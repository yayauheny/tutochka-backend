package yayauheny.by.service

import yayauheny.by.model.CountryCreateDto
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.repository.CountryRepository
import java.time.Instant
import java.util.UUID

class CountryService(private val countryRepository: CountryRepository) {
    
    suspend fun getAllCountries(): List<CountryResponseDto> = 
        countryRepository.findAll()
    
    suspend fun getCountryById(id: UUID): CountryResponseDto? = 
        countryRepository.findById(id)
    
    suspend fun getCountryByCode(code: String): CountryResponseDto? = 
        countryRepository.findByCode(code)
    
    suspend fun createCountry(createDto: CountryCreateDto): CountryResponseDto {
        if (countryRepository.existsByCode(createDto.code)) {
            throw IllegalArgumentException("Country with code '${createDto.code}' already exists")
        }
        
        val now = Instant.now()
        val countryDto = createDto.toResponseDto(UUID.randomUUID(), now, now)
        return countryRepository.save(countryDto)
    }
    
    suspend fun updateCountry(id: UUID, updateDto: CountryCreateDto): CountryResponseDto? =
        countryRepository.findById(id)?.let { existing ->
            if (updateDto.code != existing.code && countryRepository.existsByCode(updateDto.code)) {
                throw IllegalArgumentException("Country with code '${updateDto.code}' already exists")
            }
            
            val updatedDto = updateDto.toResponseDto(
                id = existing.id,
                createdAt = existing.createdAt,
                updatedAt = Instant.now()
            )
            countryRepository.save(updatedDto)
        }
    
    suspend fun deleteCountry(id: UUID): Boolean = 
        countryRepository.deleteById(id)
    
    suspend fun countryExists(code: String): Boolean = 
        countryRepository.existsByCode(code)
}

private fun CountryCreateDto.toResponseDto(
    id: UUID,
    createdAt: Instant,
    updatedAt: Instant
) = CountryResponseDto(
    id = id,
    name = name,
    code = code,
    createdAt = createdAt,
    updatedAt = updatedAt
)
