package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.SelectFieldOrAsterisk
import org.jooq.impl.DSL
import org.slf4j.LoggerFactory
import yayauheny.by.common.mapper.CityMapper
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FieldParsers
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.tables.references.CITIES
import yayauheny.by.util.pointExpr

class CityRepositoryImpl(
    private val ctx: DSLContext
) : CityRepository {
    private val logger = LoggerFactory.getLogger(CityRepositoryImpl::class.java)
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

    /**
     * Единый набор полей для SELECT/RETURNING запросов городов.
     * Включает все поля таблицы + lat/lon координаты (реальные колонки).
     */
    private fun cityProjection(): Array<SelectFieldOrAsterisk> {
        return arrayOf(
            CITIES.ID,
            CITIES.COUNTRY_ID,
            CITIES.NAME_RU,
            CITIES.NAME_EN,
            CITIES.REGION,
            CITIES.CREATED_AT,
            CITIES.UPDATED_AT,
            CITIES.IS_DELETED,
            CITIES.LAT,
            CITIES.LON
        )
    }

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*cityProjection())
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            // Сортировка по умолчанию для стабильности результатов
            // QueryExecutor.applySorting добавит дополнительную сортировку, если указана в запросе
            val baseQueryWithDefaultSort = baseQuery.orderBy(CITIES.CREATED_AT.desc(), CITIES.ID.asc())
            // Приводим к SelectConditionStep для совместимости с executePaginated
            val baseQueryCondition = baseQueryWithDefaultSort as org.jooq.SelectConditionStep<*>
            executor.executePaginated(
                baseQuery = baseQueryCondition,
                request = pagination,
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findById(id: UUID): CityResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*cityProjection())
                .from(CITIES)
                .where(
                    CITIES.ID
                        .eq(id)
                        .and(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
                ).fetchOne()
                ?.map { CityMapper.mapFromRecord(it) }
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): CityResponseDto? =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*cityProjection())
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executeSingle(
                baseQuery = baseQuery,
                filters = filters,
                builder = cityQueryBuilder
            )
        }

    override suspend fun save(dto: CityCreateDto): CityResponseDto =
        withContext(Dispatchers.IO) {
            logger.info("save() called: nameEn=${dto.nameEn}, countryId=${dto.countryId}")
            try {
                val r = CITIES
                val id = UUID.randomUUID()
                val now = Instant.now()

                val rec =
                    ctx
                        .insertInto(r)
                        .set(r.ID, id)
                        .set(r.COUNTRY_ID, dto.countryId)
                        .set(r.NAME_RU, dto.nameRu)
                        .set(r.NAME_EN, dto.nameEn)
                        .set(r.REGION, dto.region)
                        .set(
                            r.COORDINATES,
                            pointExpr(dto.coordinates.lon, dto.coordinates.lat, r.COORDINATES)
                        ).set(r.CREATED_AT, now)
                        .set(r.UPDATED_AT, now)
                        .returning(*cityProjection())
                        .fetchOne() ?: error("Error during save city: ${dto.nameEn}")

                val result = CityMapper.mapFromRecord(rec)
                logger.info("Mapping successful: id=${result.id}, nameRu=${result.nameRu}, nameEn=${result.nameEn}")
                return@withContext result
            } catch (e: Exception) {
                logger.error("Error in save()", e)
                throw e
            }
        }

    override suspend fun update(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto =
        withContext(Dispatchers.IO) {
            val r = CITIES
            val query = ctx.update(r)
            val updateStep = CityMapper.applyUpdateDto(query, updateDto)
            val rec =
                updateStep
                    .set(
                        r.COORDINATES,
                        pointExpr(updateDto.coordinates.lon, updateDto.coordinates.lat, r.COORDINATES)
                    ).set(r.UPDATED_AT, Instant.now())
                    .where(r.ID.eq(id))
                    .returning(*cityProjection())
                    .fetchOne()
                    ?: error("Failed to update city $id")

            CityMapper.mapFromRecord(rec)
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

    override suspend fun findByCountryId(
        countryId: UUID,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val countryFilter = FilterCriteria("countryId", FilterOperator.EQ, countryId.toString())
            val filters = pagination.filters + countryFilter
            val baseQuery =
                ctx
                    .select(*cityProjection())
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
                    .orderBy(CITIES.CREATED_AT.desc(), CITIES.ID.asc())
            executor.executePaginated(
                baseQuery = baseQuery as org.jooq.SelectConditionStep<*>,
                request = pagination.copy(filters = filters),
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }

    /**
     * Экранирует специальные символы для использования в LIKE запросах
     */
    private fun escapeLike(s: String): String = s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_")

    override suspend fun findByName(
        name: String,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val escapedName = escapeLike(name)
            val searchPattern = "%$escapedName%"
            val condition =
                DSL
                    .or(
                        CITIES.NAME_RU.likeIgnoreCase(searchPattern).escape('\\'),
                        CITIES.NAME_EN.likeIgnoreCase(searchPattern).escape('\\')
                    ).and(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))

            val baseQuery =
                ctx
                    .select(*cityProjection())
                    .from(CITIES)
                    .where(condition)
                    .orderBy(CITIES.CREATED_AT.desc(), CITIES.ID.asc())
            executor.executePaginated(
                baseQuery = baseQuery as org.jooq.SelectConditionStep<*>,
                request = pagination,
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }
}
