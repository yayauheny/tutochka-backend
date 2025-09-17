package yayauheny.by.repository

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or
import yayauheny.by.entity.CityEntity
import yayauheny.by.entity.CountryEntity
import yayauheny.by.model.CityResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.table.CitiesTable
import java.time.Instant
import java.util.UUID

interface CityRepository {
    suspend fun findAll(pagination: PaginationDto): PageResponseDto<CityResponseDto>
    suspend fun findById(id: UUID): CityResponseDto?
    suspend fun findByCountryId(countryId: UUID, pagination: PaginationDto): PageResponseDto<CityResponseDto>
    suspend fun findByName(name: String, pagination: PaginationDto): PageResponseDto<CityResponseDto>
    suspend fun save(city: CityResponseDto): CityResponseDto
    suspend fun deleteById(id: UUID): Boolean
    suspend fun existsByCountryAndName(countryId: UUID, name: String): Boolean
}

class CityRepositoryImpl : CityRepository {
    
    override suspend fun findAll(pagination: PaginationDto): PageResponseDto<CityResponseDto> = 
        newSuspendedTransaction { 
            val totalCount = CityEntity.all().count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size
            
            val content = CityEntity.all()
                .limit(pagination.size)
                .offset(offset.toLong())
                .map { it.toResponseDto() }
            
            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }
    
    override suspend fun findById(id: UUID): CityResponseDto? = 
        newSuspendedTransaction { 
            CityEntity.findById(id)?.toResponseDto() 
        }
    
    override suspend fun findByCountryId(countryId: UUID, pagination: PaginationDto): PageResponseDto<CityResponseDto> = 
        newSuspendedTransaction { 
            val query = CityEntity.find { CitiesTable.country eq countryId }
            val totalCount = query.count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size
            
            val content = query
                .limit(pagination.size)
                .offset(offset.toLong())
                .map { it.toResponseDto() }
            
            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
        }
    
    override suspend fun findByName(name: String, pagination: PaginationDto): PageResponseDto<CityResponseDto> = 
        newSuspendedTransaction { 
            val query = CityEntity.find { 
                (CitiesTable.nameRu like "%$name%") or (CitiesTable.nameEn like "%$name%")
            }
            val totalCount = query.count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size
            
            val content = query
                .limit(pagination.size)
                .offset(offset.toLong())
                .map { it.toResponseDto() }
            
            PageResponseDto(
                content = content,
                page = pagination.page,
                size = pagination.size,
                totalElements = totalCount,
                totalPages = totalPages,
                first = pagination.page == 0,
                last = pagination.page >= totalPages - 1
            )
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
        lat = lat!!,
        lon = lon!!
    )
}
