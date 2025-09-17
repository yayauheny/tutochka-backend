package yayauheny.by.service

import yayauheny.by.model.CityCreateDto
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.repository.CountryRepository
import java.util.UUID

class CityService(
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository
) {
    
    suspend fun getAllCities(pagination: PaginationDto): PageResponseDto<CityResponseDto> =
        cityRepository.findAll(pagination)
    
    suspend fun getCityById(id: UUID): CityResponseDto? = 
        cityRepository.findById(id)
    
    suspend fun getCitiesByCountry(countryId: UUID, pagination: PaginationDto): PageResponseDto<CityResponseDto> =
        cityRepository.findByCountryId(countryId, pagination)
    
    suspend fun searchCitiesByName(name: String, pagination: PaginationDto): PageResponseDto<CityResponseDto> =
        cityRepository.findByName(name, pagination)
    
    suspend fun createCity(createDto: CityCreateDto): CityResponseDto {
        countryRepository.findById(createDto.countryId) ?: 
            throw IllegalArgumentException("Country with ID '${createDto.countryId}' not found")
        
        if (cityRepository.existsByCountryAndName(createDto.countryId, createDto.nameRu) || 
            cityRepository.existsByCountryAndName(createDto.countryId, createDto.nameEn)) {
            throw IllegalArgumentException("City with this name already exists in the country")
        }
        
        val cityDto = createDto.toResponseDto(UUID.randomUUID())
        return cityRepository.save(cityDto)
    }
    
    suspend fun updateCity(id: UUID, updateDto: CityCreateDto): CityResponseDto? =
        cityRepository.findById(id)?.let { existing ->
            countryRepository.findById(updateDto.countryId) ?: 
                throw IllegalArgumentException("Country with ID '${updateDto.countryId}' not found")
            
            if (updateDto.nameRu != existing.nameRu && 
                cityRepository.existsByCountryAndName(updateDto.countryId, updateDto.nameRu)) {
                throw IllegalArgumentException("City with Russian name '${updateDto.nameRu}' already exists in the country")
            }
            
            if (updateDto.nameEn != existing.nameEn && 
                cityRepository.existsByCountryAndName(updateDto.countryId, updateDto.nameEn)) {
                throw IllegalArgumentException("City with English name '${updateDto.nameEn}' already exists in the country")
            }
            
            val updatedDto = updateDto.toResponseDto(id = existing.id)
            cityRepository.save(updatedDto)
        }
    
    suspend fun deleteCity(id: UUID): Boolean = 
        cityRepository.deleteById(id)
}

private fun CityCreateDto.toResponseDto(id: UUID) = CityResponseDto(
    id = id,
    countryId = countryId,
    nameRu = nameRu,
    nameEn = nameEn,
    region = region,
    lat = lat,
    lon = lon
)
