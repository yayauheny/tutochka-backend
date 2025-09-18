package yayauheny.by.repository

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import yayauheny.by.entity.CountryEntity
import yayauheny.by.model.CountryResponseDto
import yayauheny.by.model.PageResponseDto
import yayauheny.by.model.PaginationDto
import yayauheny.by.table.CountriesTable
import java.util.UUID

interface CountryRepository {
    suspend fun findAll(pagination: PaginationDto): PageResponseDto<CountryResponseDto>

    suspend fun findById(id: UUID): CountryResponseDto?

    suspend fun findByCode(code: String): CountryResponseDto?

    suspend fun save(country: CountryResponseDto): CountryResponseDto

    suspend fun deleteById(id: UUID): Boolean

    suspend fun existsByCode(code: String): Boolean
}

class CountryRepositoryImpl : CountryRepository {
    override suspend fun findAll(pagination: PaginationDto): PageResponseDto<CountryResponseDto> =
        newSuspendedTransaction {
            val totalCount = CountryEntity.all().count()
            val totalPages = if (totalCount == 0L) 0 else ((totalCount - 1) / pagination.size + 1).toInt()
            val offset = pagination.page * pagination.size

            val content =
                CountryEntity
                    .all()
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

    override suspend fun findById(id: UUID): CountryResponseDto? =
        newSuspendedTransaction {
            CountryEntity.findById(id)?.toResponseDto()
        }

    override suspend fun findByCode(code: String): CountryResponseDto? =
        newSuspendedTransaction {
            CountryEntity
                .find { CountriesTable.code eq code }
                .firstOrNull()
                ?.toResponseDto()
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

    private fun createNewEntity(dto: CountryResponseDto) =
        CountryEntity.new {
            updateFromDto(dto)
        }

    private fun CountryEntity.updateFromDto(dto: CountryResponseDto) {
        code = dto.code
        nameRu = dto.nameRu
        nameEn = dto.nameEn
    }

    private fun CountryEntity.toResponseDto() =
        CountryResponseDto(
            id = id.value,
            nameRu = nameRu,
            nameEn = nameEn,
            code = code
        )
}
