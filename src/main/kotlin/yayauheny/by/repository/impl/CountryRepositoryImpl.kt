package yayauheny.by.repository.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.common.mapper.CountryMapper
import yayauheny.by.common.query.*
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryResponseDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.repository.CountryRepository
import yayauheny.by.tables.references.COUNTRIES
import java.util.*

class CountryRepositoryImpl(
    private val ctx: DSLContext
) : CountryRepository {
    private val countryFields =
        mapOf(
            "id" to
                FieldMeta(
                    field = COUNTRIES.ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "code" to
                FieldMeta(
                    field = COUNTRIES.CODE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.EQ,
                            FilterOperator.ILIKE,
                            FilterOperator.LIKE
                        )
                ),
            "nameRu" to
                FieldMeta(
                    field = COUNTRIES.NAME_RU,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.EQ,
                            FilterOperator.ILIKE,
                            FilterOperator.LIKE
                        )
                ),
            "nameEn" to
                FieldMeta(
                    field = COUNTRIES.NAME_EN,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.EQ,
                            FilterOperator.ILIKE,
                            FilterOperator.LIKE
                        )
                ),
            "createdAt" to
                FieldMeta(
                    field = COUNTRIES.CREATED_AT,
                    parser = FieldParsers.instant,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.EQ,
                            FilterOperator.GT,
                            FilterOperator.GE,
                            FilterOperator.LT,
                            FilterOperator.LE
                        )
                ),
            "updatedAt" to
                FieldMeta(
                    field = COUNTRIES.UPDATED_AT,
                    parser = FieldParsers.instant,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.GT,
                            FilterOperator.GE,
                            FilterOperator.LT,
                            FilterOperator.LE
                        )
                )
        )

    private val countryQueryBuilder = QueryBuilder(fields = countryFields)
    private val executor = QueryExecutor(ctx, CountryMapper::mapFromRecord)

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<CountryResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .selectFrom(COUNTRIES)
                    .where(COUNTRIES.IS_DELETED.eq(false).or(COUNTRIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination,
                builder = countryQueryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findById(id: UUID): CountryResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .selectFrom(COUNTRIES)
                .where(
                    COUNTRIES.ID
                        .eq(id)
                        .and(COUNTRIES.IS_DELETED.eq(false).or(COUNTRIES.IS_DELETED.isNull))
                ).fetchOne()
                ?.map { CountryMapper.mapFromRecord(it) }
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): CountryResponseDto? =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .selectFrom(COUNTRIES)
                    .where(COUNTRIES.IS_DELETED.eq(false).or(COUNTRIES.IS_DELETED.isNull))
            executor.executeSingle(
                baseQuery = baseQuery,
                filters = filters,
                builder = countryQueryBuilder
            )
        }

    override suspend fun save(createDto: CountryCreateDto): CountryResponseDto =
        withContext(Dispatchers.IO) {
            val record = CountryMapper.mapToSaveRecord(ctx, createDto)
            val inserted = record.insert()

            if (inserted == 0) {
                throw IllegalStateException("Error during save country with code: ${createDto.code}")
            }

            CountryMapper.mapFromRecord(record)
        }

    override suspend fun update(
        id: UUID,
        updateDto: CountryUpdateDto
    ): CountryResponseDto =
        withContext(Dispatchers.IO) {
            val query = ctx.update(COUNTRIES)
            val updateStep = CountryMapper.applyUpdateDto(query, updateDto)
            updateStep
                .where(COUNTRIES.ID.eq(id))
                .returning()
                .fetchSingle()
                .map { CountryMapper.mapFromRecord(it) }
        }

    override suspend fun deleteById(id: UUID): Boolean =
        withContext(Dispatchers.IO) {
            val deleted =
                ctx
                    .update(COUNTRIES)
                    .set(COUNTRIES.IS_DELETED, true)
                    .set(COUNTRIES.DELETED_AT, java.time.Instant.now())
                    .where(COUNTRIES.ID.eq(id))
                    .execute()
            deleted > 0
        }
}
