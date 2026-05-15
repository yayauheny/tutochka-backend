package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.jooq.DSLContext
import org.jooq.JSONB
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
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
import yayauheny.by.util.toJSONB

class BuildingRepositoryImpl(
    private val ctx: DSLContext
) : BuildingRepository {
    data class ImportedBuildingUpsertResult(
        val building: BuildingResponseDto,
        val created: Boolean
    )

    private val buildingMatchKeyField = DSL.field(DSL.name("building_match_key"), SQLDataType.VARCHAR(64))
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

    private fun notDeletedCondition() = BUILDINGS.IS_DELETED.isFalse

    private fun fetchById(
        ctx: DSLContext,
        id: UUID
    ): BuildingResponseDto? =
        ctx
            .select(*projection())
            .from(BUILDINGS)
            .where(BUILDINGS.ID.eq(id).and(notDeletedCondition()))
            .fetchOne()
            ?.let { BuildingMapper.mapFromRecord(it) }

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

    suspend fun findByExternalId(
        provider: String,
        externalId: String
    ): BuildingResponseDto? =
        withContext(Dispatchers.IO) {
            findByExternalIdsInTx(ctx, provider, listOf(externalId)).firstOrNull()
        }

    suspend fun findByExternalIds(
        provider: String,
        externalIds: Collection<String>
    ): List<BuildingResponseDto> =
        withContext(Dispatchers.IO) {
            findByExternalIdsInTx(ctx, provider, externalIds)
        }

    suspend fun findByMatchKeys(matchKeys: Collection<String>): List<BuildingResponseDto> =
        withContext(Dispatchers.IO) {
            findByMatchKeysInTx(ctx, matchKeys)
        }

    suspend fun upsertImportedBuilding(
        provider: String,
        externalId: String,
        createDto: BuildingCreateDto,
        matchKey: String?
    ): ImportedBuildingUpsertResult =
        withContext(Dispatchers.IO) {
            upsertImportedBuildingInTx(ctx, provider, externalId, createDto, matchKey)
        }

    suspend fun linkExternalId(
        buildingId: UUID,
        provider: String,
        externalId: String
    ): BuildingResponseDto =
        withContext(Dispatchers.IO) {
            linkExternalIdInTx(ctx, buildingId, provider, externalId)
        }

    override suspend fun save(createDto: BuildingCreateDto): BuildingResponseDto =
        ctx.transactionSuspend { txCtx ->
            val id = UUID.randomUUID()
            val now = Instant.now()
            BuildingMapper
                .mapToSaveRecord(txCtx, createDto, id, now)
                .execute()
            fetchById(txCtx, id) ?: throw EntityNotFoundException("Здание", "не удалось сохранить")
        }

    override suspend fun update(
        id: UUID,
        updateDto: BuildingUpdateDto
    ): BuildingResponseDto =
        ctx.transactionSuspend { txCtx ->
            val updated =
                BuildingMapper
                    .applyUpdateDto(txCtx.update(BUILDINGS), updateDto)
                    .where(BUILDINGS.ID.eq(id).and(notDeletedCondition()))
                    .execute()
            if (updated == 0) {
                throw EntityNotFoundException("Здание", id.toString())
            }
            fetchById(txCtx, id) ?: throw EntityNotFoundException("Здание", id.toString())
        }

    override suspend fun deleteById(id: UUID): Boolean =
        ctx.transactionSuspend { txCtx ->
            val deleted =
                txCtx
                    .update(BUILDINGS)
                    .set(BUILDINGS.IS_DELETED, true)
                    .set(BUILDINGS.UPDATED_AT, Instant.now())
                    .set(BUILDINGS.DELETED_AT, Instant.now())
                    .where(BUILDINGS.ID.eq(id))
                    .execute()
            deleted > 0
        }

    fun findByExternalIdsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<BuildingResponseDto> {
        if (externalIds.isEmpty()) {
            return emptyList()
        }

        val providerExternalIdField =
            DSL.field(
                "({0} ->> {1})",
                SQLDataType.VARCHAR,
                BUILDINGS.EXTERNAL_IDS,
                DSL.inline(provider)
            )

        return txCtx
            .select(*projection())
            .from(BUILDINGS)
            .where(
                notDeletedCondition().and(
                    providerExternalIdField.`in`(externalIds)
                )
            ).fetch()
            .map(BuildingMapper::mapFromRecord)
    }

    fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<BuildingResponseDto> {
        if (matchKeys.isEmpty()) {
            return emptyList()
        }

        return txCtx
            .select(*projection())
            .from(BUILDINGS)
            .where(
                notDeletedCondition().and(buildingMatchKeyField.`in`(matchKeys))
            ).fetch()
            .map(BuildingMapper::mapFromRecord)
    }

    fun upsertImportedBuildingInTx(
        txCtx: DSLContext,
        provider: String,
        externalId: String,
        createDto: BuildingCreateDto,
        matchKey: String?
    ): ImportedBuildingUpsertResult {
        val now = Instant.now()
        val id = UUID.randomUUID()
        val providerExternalIds = mergeProviderJson(null, provider, externalId)
        val insertedField = DSL.field("xmax = 0", SQLDataType.BOOLEAN).`as`("inserted")

        val insertStep =
            txCtx
                .insertInto(BUILDINGS)
                .set(BUILDINGS.ID, id)
                .set(BUILDINGS.CITY_ID, createDto.cityId)
                .set(BUILDINGS.NAME, createDto.name)
                .set(BUILDINGS.ADDRESS, createDto.address.takeIf { it.isNotBlank() })
                .set(BUILDINGS.BUILDING_TYPE, createDto.buildingType?.code)
                .set(BUILDINGS.WORK_TIME, createDto.workTime.toJSONB())
                .set(
                    BUILDINGS.COORDINATES,
                    yayauheny.by.util.pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, BUILDINGS.COORDINATES)
                ).set(BUILDINGS.EXTERNAL_IDS, providerExternalIds.toJSONB())
                .set(BUILDINGS.IMPORT_STATUS, createDto.importStatus.name)
                .set(BUILDINGS.CREATED_AT, now)
                .set(BUILDINGS.UPDATED_AT, now)
        val insertWithMatchKey =
            if (matchKey != null) {
                insertStep.set(buildingMatchKeyField, matchKey)
            } else {
                insertStep
            }
        val updateStep =
            insertWithMatchKey
                .onConflict(buildingMatchKeyField)
                .doUpdate()
                .set(BUILDINGS.CITY_ID, createDto.cityId)
                .set(BUILDINGS.NAME, createDto.name)
                .set(BUILDINGS.ADDRESS, createDto.address.takeIf { it.isNotBlank() })
                .set(BUILDINGS.BUILDING_TYPE, createDto.buildingType?.code)
                .set(BUILDINGS.WORK_TIME, createDto.workTime.toJSONB())
                .set(
                    BUILDINGS.COORDINATES,
                    yayauheny.by.util.pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, BUILDINGS.COORDINATES)
                ).set(BUILDINGS.EXTERNAL_IDS, mergeProviderJsonField(BUILDINGS.EXTERNAL_IDS, provider, externalId))
                .set(BUILDINGS.UPDATED_AT, now)
        val record =
            (
                if (matchKey != null) {
                    updateStep.set(buildingMatchKeyField, matchKey)
                } else {
                    updateStep
                }
            ).returningResult(*((projection().toList()) + insertedField).toTypedArray())
                .fetchOne()
                ?: throw EntityNotFoundException("Здание", "не удалось сохранить")

        val building = BuildingMapper.mapFromRecord(record)
        val created = record.get("inserted", Boolean::class.java) ?: false
        return ImportedBuildingUpsertResult(building = building, created = created)
    }

    fun linkExternalIdInTx(
        txCtx: DSLContext,
        buildingId: UUID,
        provider: String,
        externalId: String
    ): BuildingResponseDto {
        txCtx
            .update(BUILDINGS)
            .set(BUILDINGS.EXTERNAL_IDS, mergeProviderJsonField(BUILDINGS.EXTERNAL_IDS, provider, externalId))
            .set(BUILDINGS.UPDATED_AT, Instant.now())
            .where(BUILDINGS.ID.eq(buildingId).and(notDeletedCondition()))
            .execute()

        return fetchById(txCtx, buildingId) ?: throw EntityNotFoundException("Здание", buildingId.toString())
    }

    private fun mergeProviderJson(
        current: JsonObject?,
        provider: String,
        externalId: String
    ): JsonObject =
        buildJsonObject {
            current?.forEach { (key, value) -> put(key, value) }
            put(provider, JsonPrimitive(externalId))
        }

    private fun mergeProviderJsonField(
        field: org.jooq.TableField<*, JSONB?>,
        provider: String,
        externalId: String
    ) = DSL.field(
        "COALESCE({0}, '{}'::jsonb) || jsonb_build_object({1}, {2})",
        SQLDataType.JSONB,
        field,
        DSL.inline(provider),
        DSL.inline(externalId)
    )
}
