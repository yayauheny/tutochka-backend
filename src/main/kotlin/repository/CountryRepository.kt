package yayauheny.by.repository

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import yayauheny.by.entity.CountryEntity
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.table.CountriesTable
import java.time.Instant
import java.util.UUID

interface CountryRepository {
    suspend fun findAll(): List<CountryResponseDto>
    suspend fun findById(id: UUID): CountryResponseDto?
    suspend fun findByCode(code: String): CountryResponseDto?
    suspend fun save(country: CountryResponseDto): CountryResponseDto
    suspend fun deleteById(id: UUID): Boolean
    suspend fun existsByCode(code: String): Boolean
}

class CountryRepositoryImpl : CountryRepository {
    
    override suspend fun findAll(): List<CountryResponseDto> = 
        newSuspendedTransaction { 
            CountryEntity.all().map { it.toResponseDto() } 
        }
    
    override suspend fun findById(id: UUID): CountryResponseDto? = 
        newSuspendedTransaction { 
            CountryEntity.findById(id)?.toResponseDto() 
        }
    
    override suspend fun findByCode(code: String): CountryResponseDto? = 
        newSuspendedTransaction { 
            CountryEntity.find { CountriesTable.code eq code }
                .firstOrNull()?.toResponseDto()
        }
    
    override suspend fun save(country: CountryResponseDto): CountryResponseDto = 
        newSuspendedTransaction {
            val entity = CountryEntity.findById(country.id) ?: createNewEntity(country)
            entity.updateFromDto(country)
            entity.toResponseDto()
        }
    
    override suspend fun deleteById(id: UUID): Boolean = 
        newSuspendedTransaction { 
            CountryEntity.findById(id)?.delete() != null 
        }
    
    override suspend fun existsByCode(code: String): Boolean = 
        newSuspendedTransaction { 
            CountryEntity.find { CountriesTable.code eq code }.count() > 0
        }
    
    private fun createNewEntity(dto: CountryResponseDto) = CountryEntity.new {
        updateFromDto(dto)
    }
    
    private fun CountryEntity.updateFromDto(dto: CountryResponseDto) {
        code = dto.code
        name = dto.name
    }
    
    private fun CountryEntity.toResponseDto() = CountryResponseDto(
        id = id.value,
        name = name,
        code = code,
        createdAt = Instant.now(),
        updatedAt = Instant.now()
    )
}
