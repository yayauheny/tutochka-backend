package yayauheny.by.repository

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.model.CityResponseDto
import yayauheny.by.table.CitiesTable
import java.time.Instant
import java.util.UUID

interface CityRepository {
    suspend fun findAll(): List<CityResponseDto>
    suspend fun findById(id: UUID): CityResponseDto?
    suspend fun findByCountryId(countryId: UUID): List<CityResponseDto>
    suspend fun findByName(name: String): List<CityResponseDto>
    suspend fun save(city: CityResponseDto): CityResponseDto
    suspend fun deleteById(id: UUID): Boolean
    suspend fun existsByCountryAndName(countryId: UUID, name: String): Boolean
}

class CityRepositoryImpl : CityRepository {
    
    override suspend fun findAll(): List<CityResponseDto> = 
        newSuspendedTransaction { 
            CityEntity.all().map { it.toResponseDto() } 
        }
    
    override suspend fun findById(id: UUID): CityResponseDto? = 
        newSuspendedTransaction { 
            CityEntity.findById(id)?.toResponseDto() 
        }
    
    override suspend fun findByCountryId(countryId: UUID): List<CityResponseDto> = 
        newSuspendedTransaction { 
            CityEntity.find { CitiesTable.country eq countryId }
                .map { it.toResponseDto() }
        }
    
    override suspend fun findByName(name: String): List<CityResponseDto> = 
        newSuspendedTransaction { 
            CityEntity.find { 
                (CitiesTable.nameRu like "%$name%") or (CitiesTable.nameEn like "%$name%")
            }.map { it.toResponseDto() }
        }
    
    override suspend fun save(city: CityResponseDto): CityResponseDto = 
        newSuspendedTransaction {
            val entity = CityEntity.findById(city.id) ?: createNewEntity(city)
            entity.updateFromDto(city)
            entity.toResponseDto()
        }
    
    override suspend fun deleteById(id: UUID): Boolean = 
        newSuspendedTransaction { 
            CityEntity.findById(id)?.delete() != null 
        }
    
    override suspend fun existsByCountryAndName(countryId: UUID, name: String): Boolean = 
        newSuspendedTransaction { 
            CityEntity.find { 
                (CitiesTable.country eq countryId) and 
                ((CitiesTable.nameRu eq name) or (CitiesTable.nameEn eq name))
            }.count() > 0
        }
    
    private fun createNewEntity(dto: CityResponseDto) = CityEntity.new {
        updateFromDto(dto)
    }
    
    private fun CityEntity.updateFromDto(dto: CityResponseDto) {
        country = CountryEntity.findById(dto.countryId)!!
        nameRu = dto.nameRu
        nameEn = dto.nameEn
        region = dto.region
        lat = dto.lat
        lon = dto.lon
    }
    
    private fun CityEntity.toResponseDto() = CityResponseDto(
        id = id.value,
        countryId = country.id.value,
        nameRu = nameRu,
        nameEn = nameEn,
        region = region,
        lat = lat,
        lon = lon,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}
