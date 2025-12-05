package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.mapper.BuildingMapper
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FieldParsers
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.building.BuildingUpdateDto
import yayauheny.by.repository.BuildingRepository
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.transactionSuspend

class BuildingRepositoryImpl(
    private val ctx: DSLContext
) : BuildingRepository {
    private val buildingFields =
        mapOf(
            "id" to
                FieldMeta(
                    field = BUILDINGS.ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "cityId" to
                FieldMeta(
                    field = BUILDINGS.CITY_ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "name" to
                FieldMeta(
                    field = BUILDINGS.NAME,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.ILIKE, FilterOperator.LIKE)
                ),
            "address" to
                FieldMeta(
                    field = BUILDINGS.ADDRESS,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.ILIKE, FilterOperator.LIKE)
                ),
            "buildingType" to
                FieldMeta(
                    field = BUILDINGS.BUILDING_TYPE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "createdAt" to
                FieldMeta(
                    field = BUILDINGS.CREATED_AT,
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
                    field = BUILDINGS.UPDATED_AT,
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

    private val queryBuilder = QueryBuilder(fields = buildingFields)
    private val executor = QueryExecutor(ctx, BuildingMapper::mapFromRecord)

    /**
     * Возвращает все поля таблицы BUILDINGS кроме coordinates (для SELECT запросов)
     */
    private fun getAllBuildingsFieldsExceptCoordinates(): List<org.jooq.Field<*>> {
        val b = BUILDINGS
        return listOf(
            b.ID,
            b.CITY_ID,
            b.NAME,
            b.ADDRESS,
            b.BUILDING_TYPE,
            b.WORK_TIME,
            b.EXTERNAL_IDS,
            b.IS_DELETED,
            b.CREATED_AT,
            b.UPDATED_AT
        )
    }

    /**
     * Возвращает все поля таблицы BUILDINGS с координатами (lat/lon) для SELECT запросов.
     */
    private fun getAllBuildingsFieldsWithCoordinates(): List<org.jooq.Field<*>> {
        val b = BUILDINGS
        return getAllBuildingsFieldsExceptCoordinates() + b.COORDINATES.latAlias() + b.COORDINATES.lonAlias()
    }

    private fun projection(): Array<org.jooq.SelectFieldOrAsterisk> = getAllBuildingsFieldsWithCoordinates().toTypedArray()

    private fun notDeletedCondition() = BUILDINGS.IS_DELETED.eq(false).or(BUILDINGS.IS_DELETED.isNull)

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<BuildingResponseDto> =
        withContext(Dispatchers.IO) {
            val base =
                ctx
                    .select(*projection())
                    .from(BUILDINGS)
                    .where(notDeletedCondition())
            executor.executePaginated(
                baseQuery = base,
                request = pagination,
                builder = queryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): BuildingResponseDto? =
        withContext(Dispatchers.IO) {
            val base =
                ctx
                    .select(*projection())
                    .from(BUILDINGS)
                    .where(notDeletedCondition())
            executor.executeSingle(
                baseQuery = base,
                filters = filters,
                builder = queryBuilder
            )
        }

    override suspend fun findById(id: UUID): BuildingResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*projection())
                .from(BUILDINGS)
                .where(BUILDINGS.ID.eq(id).and(notDeletedCondition()))
                .fetchOne()
                ?.let { BuildingMapper.mapFromRecord(it) }
        }

    override suspend fun findByExternalId(
        provider: String,
        externalId: String
    ): BuildingResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*projection())
                .from(BUILDINGS)
                .where(
                    notDeletedCondition().and(
                        DSL.condition("{0} ->> {1} = {2}", BUILDINGS.EXTERNAL_IDS, provider, externalId)
                    )
                ).orderBy(BUILDINGS.CREATED_AT.desc(), BUILDINGS.ID.desc())
                .limit(1)
                .fetchOne()
                ?.let { BuildingMapper.mapFromRecord(it) }
        }

    override suspend fun save(createDto: BuildingCreateDto): BuildingResponseDto =
        ctx.transactionSuspend { txCtx ->
            val id = UUID.randomUUID()
            val now = Instant.now()
            val rec =
                BuildingMapper
                    .mapToSaveRecord(txCtx, createDto, id, now)
                    .returning(*projection())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Здание", "не удалось сохранить")
            BuildingMapper.mapFromRecord(rec)
        }

    override suspend fun update(
        id: UUID,
        updateDto: BuildingUpdateDto
    ): BuildingResponseDto =
        ctx.transactionSuspend { txCtx ->
            val rec =
                BuildingMapper
                    .applyUpdateDto(txCtx.update(BUILDINGS), updateDto)
                    .where(BUILDINGS.ID.eq(id).and(notDeletedCondition()))
                    .returning(*projection())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Здание", id.toString())
            BuildingMapper.mapFromRecord(rec)
        }

    override suspend fun deleteById(id: UUID): Boolean =
        ctx.transactionSuspend { txCtx ->
            val deleted =
                txCtx
                    .update(BUILDINGS)
                    .set(BUILDINGS.IS_DELETED, true)
                    .set(BUILDINGS.UPDATED_AT, Instant.now())
                    .execute()
            deleted > 0
        }
}
