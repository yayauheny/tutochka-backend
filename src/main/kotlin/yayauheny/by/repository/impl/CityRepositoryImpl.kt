package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.SelectFieldOrAsterisk
import org.jooq.impl.DSL
import yayauheny.by.common.errors.EntityNotFoundException
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
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.pointExpr
import yayauheny.by.util.transactionSuspend

class CityRepositoryImpl(
    private val ctx: DSLContext
) : CityRepository {
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
     * Включает все поля таблицы + lat/lon координаты.
     */
    private fun cityProjection(): Array<SelectFieldOrAsterisk> {
        val latF = CITIES.COORDINATES.latAlias()
        val lonF = CITIES.COORDINATES.lonAlias()

        // Не используем CITIES.asterisk(), потому что оно включает coordinates (Geometry),
        // который jOOQ не может правильно прочитать без специальной обработки.
        // Используем только вычисляемые поля lat/lon через ST_X/ST_Y
        return arrayOf(
            CITIES.ID,
            CITIES.COUNTRY_ID,
            CITIES.NAME_RU,
            CITIES.NAME_EN,
            CITIES.REGION,
            // CITIES.COORDINATES - не включаем, используем только lat/lon через ST_X/ST_Y
            CITIES.CREATED_AT,
            CITIES.UPDATED_AT,
            CITIES.IS_DELETED,
            latF,
            lonF
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

    private fun buildInsertQuery(
        txCtx: DSLContext,
        dto: CityCreateDto,
        id: UUID,
        now: Instant
    ) = txCtx
        .insertInto(CITIES)
        .set(CITIES.ID, id)
        .set(CITIES.COUNTRY_ID, dto.countryId)
        .set(CITIES.NAME_RU, dto.nameRu)
        .set(CITIES.NAME_EN, dto.nameEn)
        .set(CITIES.REGION, dto.region)
        .set(
            CITIES.COORDINATES,
            pointExpr(dto.coordinates.lon, dto.coordinates.lat, CITIES.COORDINATES)
        ).set(CITIES.CREATED_AT, now)
        .set(CITIES.UPDATED_AT, now)

    override suspend fun save(dto: CityCreateDto): CityResponseDto =
        ctx.transactionSuspend { txCtx ->
            val r = CITIES
            val id = UUID.randomUUID()
            val now = Instant.now()

            // План Б: сначала возвращаем только ID, затем делаем отдельный SELECT
            // Это необходимо, потому что jOOQ не может вернуть вычисляемые поля (lat/lon через ST_X/ST_Y) в RETURNING
            val idRec =
                buildInsertQuery(txCtx, dto, id, now)
                    .returning(r.ID)
                    .fetchOne() ?: throw EntityNotFoundException("Город", "не удалось сохранить: ${dto.nameEn}")
            val insertedId = idRec[r.ID]!!

            val rec =
                txCtx
                    .select(*cityProjection())
                    .from(r)
                    .where(r.ID.eq(insertedId))
                    .fetchOne() ?: throw EntityNotFoundException("Город", insertedId.toString())

            CityMapper.mapFromRecord(rec)
        }

    private fun buildUpdateQuery(
        txCtx: DSLContext,
        updateDto: CityUpdateDto,
        id: UUID
    ) = CityMapper
        .applyUpdateDto(txCtx.update(CITIES), updateDto)
        .set(
            CITIES.COORDINATES,
            pointExpr(updateDto.coordinates.lon, updateDto.coordinates.lat, CITIES.COORDINATES)
        ).set(CITIES.UPDATED_AT, Instant.now())
        .where(CITIES.ID.eq(id))

    override suspend fun update(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto =
        ctx.transactionSuspend { txCtx ->
            val rec =
                buildUpdateQuery(txCtx, updateDto, id)
                    .returning(*cityProjection())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Город", id.toString())

            CityMapper.mapFromRecord(rec)
        }

    override suspend fun deleteById(id: UUID): Boolean =
        ctx.transactionSuspend { txCtx ->
            val updated =
                txCtx
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
