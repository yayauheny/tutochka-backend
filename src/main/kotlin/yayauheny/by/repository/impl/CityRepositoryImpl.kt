package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.common.mapper.CityMapper
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FieldParsers
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.di.CityRepo
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.tables.references.CITIES

class CityRepositoryImpl(
    private val ctx: DSLContext
) : CityRepo {
    private val cityFields =
        mapOf(
            "id" to
                FieldMeta(
                    field = CITIES.ID,
                    parser = FieldParsers.uuid,
                    allowedOps = setOf(FilterOperator.EQ)
                ),
            "countryId" to
                FieldMeta(
                    field = CITIES.COUNTRY_ID,
                    sortable = true,
                    parser = FieldParsers.uuid,
                    allowedOps = setOf(FilterOperator.EQ)
                ),
            "nameRu" to
                FieldMeta(
                    field = CITIES.NAME_RU,
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
                    field = CITIES.NAME_EN,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps =
                        setOf(
                            FilterOperator.EQ,
                            FilterOperator.ILIKE,
                            FilterOperator.LIKE
                        )
                ),
            "region" to
                FieldMeta(
                    field = CITIES.REGION,
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
                    field = CITIES.CREATED_AT,
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
                    field = CITIES.UPDATED_AT,
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
    private val cityQueryBuilder = QueryBuilder(fields = cityFields)
    private val executor = QueryExecutor(ctx, CityMapper::mapFromRecord)

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .selectFrom(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination,
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findById(id: UUID): CityResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .selectFrom(CITIES)
                .where(
                    CITIES.ID
                        .eq(id)
                        .and(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
                )
                .fetchOne()
                ?.map { CityMapper.mapFromRecord(it) }
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): CityResponseDto? =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .selectFrom(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executeSingle(
                baseQuery = baseQuery,
                filters = filters,
                builder = cityQueryBuilder
            )
        }

    override suspend fun save(dto: CityCreateDto): CityResponseDto =
        withContext(Dispatchers.IO) {
            val record = CityMapper.mapToSaveRecord(ctx, dto)
            val inserted = record.insert()

            if (inserted == 0) {
                throw IllegalStateException("Error during save city with name: ${dto.nameEn}")
            }

            CityMapper.mapFromRecord(record)
        }

    override suspend fun update(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto =
        withContext(Dispatchers.IO) {
            val query = ctx.update(CITIES)
            val updateStep = CityMapper.applyUpdateDto(query, updateDto)
            val updated =
                updateStep
                    .set(CITIES.UPDATED_AT, Instant.now())
                    .where(CITIES.ID.eq(id))
                    .returning()
                    .fetchOne()
                    ?: throw IllegalStateException("Failed to update city with id: $id")


            CityMapper.mapFromRecord(updated)
        }

    override suspend fun deleteById(id: UUID): Boolean =
        withContext(Dispatchers.IO) {
            val updated =
                ctx
                    .update(CITIES)
                    .set(CITIES.IS_DELETED, true)
                    .set(CITIES.DELETED_AT, Instant.now())
                    .where(CITIES.ID.eq(id))
                    .execute()
            updated > 0
        }

    suspend fun findByCountryId(
        countryId: UUID,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val countryFilter = FilterCriteria("countryId", FilterOperator.EQ, countryId.toString())
            val filters = pagination.filters + countryFilter
            val baseQuery =
                ctx
                    .selectFrom(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }

    suspend fun findByName(
        name: String,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val nameFilter = FilterCriteria("nameRu", FilterOperator.ILIKE, "%$name%")
            val filters = pagination.filters + nameFilter
            val baseQuery =
                ctx
                    .selectFrom(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }
}
