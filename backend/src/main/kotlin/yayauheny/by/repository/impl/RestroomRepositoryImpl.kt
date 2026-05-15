package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.Condition
import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.JSONB
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import org.slf4j.LoggerFactory
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.mapper.RestroomMapper
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FieldParsers
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.model.restroom.NearestRestroomSlimDto
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.importing.model.ImportOriginKey
import yayauheny.by.util.ScheduleUtils
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.tables.references.CITIES
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.tables.references.SUBWAY_LINES
import yayauheny.by.tables.references.SUBWAY_STATIONS
import yayauheny.by.util.distanceGeographyTo
import yayauheny.by.util.knnOrderTo
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.pointExpr
import yayauheny.by.util.setIfNotNullCoordinates
import yayauheny.by.util.toJSONB
import yayauheny.by.util.transactionSuspend
import yayauheny.by.util.withinDistanceOf

class RestroomRepositoryImpl(
    private val ctx: DSLContext,
    private val scheduleMappingService: ScheduleMappingService? = null
) : RestroomRepository {
    data class ImportedRestroomUpsertResult(
        val restroom: RestroomResponseDto,
        val created: Boolean
    )

    private val logger = LoggerFactory.getLogger(RestroomRepositoryImpl::class.java)
    private val restroomMatchKeyField = DSL.field(DSL.name("restroom_match_key"), SQLDataType.VARCHAR(64))
    private val restroomFields =
        mapOf(
            "id" to
                FieldMeta(
                    field = RESTROOMS.ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "cityId" to
                FieldMeta(
                    field = RESTROOMS.CITY_ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN, FilterOperator.NE)
                ),
            "name" to
                FieldMeta(
                    field = RESTROOMS.NAME,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.LIKE, FilterOperator.ILIKE)
                ),
            "address" to
                FieldMeta(
                    field = RESTROOMS.ADDRESS,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.LIKE, FilterOperator.ILIKE)
                ),
            "status" to
                FieldMeta(
                    field = RESTROOMS.STATUS,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.NE, FilterOperator.IN)
                ),
            "placeType" to
                FieldMeta(
                    field = RESTROOMS.PLACE_TYPE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "feeType" to
                FieldMeta(
                    field = RESTROOMS.FEE_TYPE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "genderType" to
                FieldMeta(
                    field = DSL.field("gender_type", SQLDataType.VARCHAR(20)),
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "accessibilityType" to
                FieldMeta(
                    field = RESTROOMS.ACCESSIBILITY_TYPE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "buildingId" to
                FieldMeta(
                    field = RESTROOMS.BUILDING_ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "subwayStationId" to
                FieldMeta(
                    field = RESTROOMS.SUBWAY_STATION_ID,
                    parser = FieldParsers.uuid,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "dataSource" to
                FieldMeta(
                    field = RESTROOMS.DATA_SOURCE,
                    parser = FieldParsers.string,
                    sortable = true,
                    allowedOps = setOf(FilterOperator.EQ, FilterOperator.IN)
                ),
            "createdAt" to
                FieldMeta(
                    field = RESTROOMS.CREATED_AT,
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
                    field = RESTROOMS.UPDATED_AT,
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

    private val restroomQueryBuilder = QueryBuilder(fields = restroomFields)
    private val executor = QueryExecutor(ctx, RestroomMapper::mapFromRecord)

    /**
     * Возвращает все поля таблицы RESTROOMS кроме coordinates (для SELECT запросов)
     */
    private fun getAllRestroomsFieldsExceptCoordinates(): List<Field<*>> {
        return listOf(
            RESTROOMS.ID,
            RESTROOMS.CITY_ID,
            RESTROOMS.BUILDING_ID,
            RESTROOMS.SUBWAY_STATION_ID,
            RESTROOMS.NAME,
            RESTROOMS.ADDRESS,
            RESTROOMS.PHONES,
            RESTROOMS.WORK_TIME,
            RESTROOMS.FEE_TYPE,
            RESTROOMS.GENDER_TYPE,
            RESTROOMS.ACCESSIBILITY_TYPE,
            RESTROOMS.PLACE_TYPE,
            RESTROOMS.DATA_SOURCE,
            RESTROOMS.STATUS,
            RESTROOMS.AMENITIES,
            RESTROOMS.EXTERNAL_MAPS,
            RESTROOMS.ACCESS_NOTE,
            RESTROOMS.DIRECTION_GUIDE,
            RESTROOMS.INHERIT_BUILDING_SCHEDULE,
            RESTROOMS.HAS_PHOTOS,
            RESTROOMS.LOCATION_TYPE,
            RESTROOMS.ORIGIN_PROVIDER,
            RESTROOMS.ORIGIN_ID,
            RESTROOMS.IS_HIDDEN,
            RESTROOMS.IS_DELETED,
            RESTROOMS.CREATED_AT,
            RESTROOMS.UPDATED_AT,
            RESTROOMS.DELETED_AT
        )
    }

    /**
     * Возвращает все поля таблицы RESTROOMS с координатами (lat/lon) для SELECT запросов.
     * Устраняет дублирование кода создания latField и lonField в репозиториях.
     */
    private fun getAllRestroomsFieldsWithCoordinates(): List<Field<*>> {
        return getAllRestroomsFieldsExceptCoordinates() + RESTROOMS.COORDINATES.latAlias() + RESTROOMS.COORDINATES.lonAlias()
    }

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.isFalse)
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination,
                builder = restroomQueryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.isFalse)
            executor.executeSingle(
                baseQuery = baseQuery,
                filters = filters,
                builder = restroomQueryBuilder
            )
        }

    override suspend fun findById(id: UUID): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            val c = CITIES
            val b = BUILDINGS
            val s = SUBWAY_STATIONS
            val l = SUBWAY_LINES

            val selectFields =
                getAllRestroomsFieldsWithCoordinates() +
                    listOf(
                        c.NAME_EN.`as`("city_name_en"),
                        b.ID.`as`("b_id"),
                        b.CITY_ID.`as`("b_city_id"),
                        b.NAME.`as`("b_name"),
                        b.ADDRESS.`as`("b_address"),
                        b.BUILDING_TYPE.`as`("b_type"),
                        b.WORK_TIME.`as`("b_work_time"),
                        b.COORDINATES.latAlias().`as`("b_lat"),
                        b.COORDINATES.lonAlias().`as`("b_lon"),
                        b.EXTERNAL_IDS.`as`("b_external_ids"),
                        b.IS_DELETED.`as`("b_is_deleted"),
                        b.CREATED_AT.`as`("b_created_at"),
                        b.UPDATED_AT.`as`("b_updated_at"),
                        s.ID.`as`("s_id"),
                        s.NAME_RU.`as`("s_name_ru"),
                        s.NAME_EN.`as`("s_name_en"),
                        s.IS_TRANSFER.`as`("s_is_transfer"),
                        s.COORDINATES.latAlias().`as`("s_lat"),
                        s.COORDINATES.lonAlias().`as`("s_lon"),
                        s.IS_DELETED.`as`("s_is_deleted"),
                        s.CREATED_AT.`as`("s_created_at"),
                        l.ID.`as`("l_id"),
                        l.CITY_ID.`as`("l_city_id"),
                        l.NAME_RU.`as`("l_name_ru"),
                        l.NAME_EN.`as`("l_name_en"),
                        l.HEX_COLOR.`as`("l_hex"),
                        l.IS_DELETED.`as`("l_is_deleted"),
                        l.CREATED_AT.`as`("l_created_at")
                    )

            ctx
                .select(*selectFields.toTypedArray())
                .from(RESTROOMS)
                .leftJoin(c)
                .on(RESTROOMS.CITY_ID.eq(c.ID).and(c.IS_DELETED.isFalse))
                .leftJoin(b)
                .on(RESTROOMS.BUILDING_ID.eq(b.ID).and(b.IS_DELETED.isFalse))
                .leftJoin(s)
                .on(RESTROOMS.SUBWAY_STATION_ID.eq(s.ID).and(s.IS_DELETED.isFalse))
                .leftJoin(l)
                .on(s.SUBWAY_LINE_ID.eq(l.ID).and(l.IS_DELETED.isFalse))
                .where(
                    RESTROOMS.ID
                        .eq(id)
                        .and(RESTROOMS.IS_DELETED.isFalse)
                ).fetchOne()
                ?.let { record ->
                    RestroomMapper.mapFromRecordEnriched(record)
                }
        }

    private fun buildInsertQuery(
        txCtx: DSLContext,
        createDto: RestroomCreateDto,
        id: UUID,
        now: Instant
    ) = txCtx
        .insertInto(RESTROOMS)
        .set(RESTROOMS.ID, id)
        .set(RESTROOMS.CITY_ID, createDto.cityId)
        .set(RESTROOMS.BUILDING_ID, createDto.buildingId)
        .set(RESTROOMS.SUBWAY_STATION_ID, createDto.subwayStationId)
        .set(RESTROOMS.NAME, createDto.name)
        .set(RESTROOMS.ADDRESS, createDto.address.takeIf { !it.isNullOrBlank() && it != "null" })
        .set(RESTROOMS.PHONES, createDto.phones.toJSONB())
        .set(RESTROOMS.WORK_TIME, createDto.workTime.toJSONB())
        .set(RESTROOMS.FEE_TYPE, createDto.feeType?.name)
        .set(RESTROOMS.GENDER_TYPE, createDto.genderType?.name)
        .set(RESTROOMS.ACCESSIBILITY_TYPE, createDto.accessibilityType.name)
        .set(RESTROOMS.PLACE_TYPE, createDto.placeType.code)
        .set(
            RESTROOMS.COORDINATES,
            pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, RESTROOMS.COORDINATES)
        ).set(RESTROOMS.DATA_SOURCE, createDto.dataSource.name)
        .set(RESTROOMS.STATUS, createDto.status.name)
        .set(RESTROOMS.AMENITIES, createDto.amenities.toJSONB())
        .set(RESTROOMS.EXTERNAL_MAPS, createDto.externalMaps.toJSONB())
        .set(RESTROOMS.ACCESS_NOTE, createDto.accessNote)
        .set(RESTROOMS.DIRECTION_GUIDE, createDto.directionGuide)
        .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, createDto.inheritBuildingSchedule)
        .set(RESTROOMS.HAS_PHOTOS, createDto.hasPhotos)
        .set(RESTROOMS.LOCATION_TYPE, createDto.locationType.name)
        .set(RESTROOMS.ORIGIN_PROVIDER, createDto.originProvider.name)
        .set(RESTROOMS.ORIGIN_ID, createDto.originId)
        .set(RESTROOMS.IS_HIDDEN, createDto.isHidden)
        .set(RESTROOMS.CREATED_AT, now)
        .set(RESTROOMS.UPDATED_AT, now)

    override suspend fun save(createDto: RestroomCreateDto): RestroomResponseDto =
        ctx.transactionSuspend { txCtx ->
            val id = UUID.randomUUID()
            val now = Instant.now()

            val rec =
                buildInsertQuery(txCtx, createDto, id, now)
                    .returningResult(getAllRestroomsFieldsWithCoordinates())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Туалет", "не удалось сохранить")

            RestroomMapper.mapFromRecord(rec)
        }

    private fun buildUpdateQuery(
        txCtx: DSLContext,
        updateDto: RestroomUpdateDto,
        id: UUID
    ) = RestroomMapper
        .applyUpdateDto(txCtx.update(RESTROOMS), updateDto)
        .setIfNotNullCoordinates(RESTROOMS.COORDINATES, updateDto.coordinates)
        .set(RESTROOMS.UPDATED_AT, Instant.now())
        .where(RESTROOMS.ID.eq(id))

    override suspend fun update(
        id: UUID,
        updateDto: RestroomUpdateDto
    ): RestroomResponseDto =
        ctx.transactionSuspend { txCtx ->
            val rec =
                buildUpdateQuery(txCtx, updateDto, id)
                    .returningResult(getAllRestroomsFieldsWithCoordinates())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Туалет", id.toString())

            RestroomMapper.mapFromRecord(rec)
        }

    override suspend fun deleteById(id: UUID): Boolean =
        ctx.transactionSuspend { txCtx ->
            txCtx
                .update(RESTROOMS)
                .set(RESTROOMS.IS_DELETED, true)
                .set(RESTROOMS.DELETED_AT, Instant.now())
                .where(RESTROOMS.ID.eq(id))
                .execute() > 0
        }

    override suspend fun findNearestByLocation(
        requestLat: Double,
        requestLon: Double,
        limit: Int,
        distanceMeters: Int?
    ): List<NearestRestroomSlimDto> =
        withContext(Dispatchers.IO) {
            val maxElements = limit
            val maxDistance = (distanceMeters ?: 5000).toDouble()
            val knnField = RESTROOMS.COORDINATES.knnOrderTo(requestLat, requestLon)
            val distanceField = RESTROOMS.COORDINATES.distanceGeographyTo(requestLat, requestLon).`as`("distance")
            val selectFields =
                listOf(
                    RESTROOMS.ID,
                    RESTROOMS.NAME,
                    RESTROOMS.FEE_TYPE,
                    RESTROOMS.COORDINATES.latAlias(),
                    RESTROOMS.COORDINATES.lonAlias(),
                    distanceField
                )

            ctx
                .select(selectFields)
                .from(RESTROOMS)
                .where(
                    RESTROOMS.COORDINATES
                        .withinDistanceOf(requestLat, requestLon, maxDistance)
                        .and(RESTROOMS.STATUS.eq(RestroomStatus.ACTIVE.name))
                        .and(RESTROOMS.IS_DELETED.isFalse)
                        .and(RESTROOMS.IS_HIDDEN.eq(false))
                ).orderBy(knnField.asc())
                .limit(maxElements)
                .fetch()
                .map { RestroomMapper.mapToNearestRestroomSlim(it, requestLat, requestLon) }
        }

    override suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationRequest
    ): PageResponse<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val cityFilter = FilterCriteria("cityId", FilterOperator.EQ, cityId.toString())
            val filters = pagination.filters + cityFilter
            val baseQuery =
                ctx
                    .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.isFalse)
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = restroomQueryBuilder,
                fetchCount = true
            )
        }

    suspend fun findByExternalMap(
        provider: String,
        externalId: String
    ): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            findByExternalMapsInTx(ctx, provider, listOf(externalId)).firstOrNull()
        }

    suspend fun findByOrigin(
        originProvider: ImportProvider,
        originId: String
    ): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            findByOriginsInTx(ctx, listOf(ImportOriginKey(originProvider, originId))).firstOrNull()
        }

    suspend fun findByOrigins(origins: Collection<ImportOriginKey>): List<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            findByOriginsInTx(ctx, origins)
        }

    suspend fun findByExternalMaps(
        provider: String,
        externalIds: Collection<String>
    ): List<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            findByExternalMapsInTx(ctx, provider, externalIds)
        }

    suspend fun findByMatchKeys(matchKeys: Collection<String>): List<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            findByMatchKeysInTx(ctx, matchKeys)
        }

    suspend fun upsertImportedRestroom(
        createDto: RestroomCreateDto,
        matchKey: String?
    ): ImportedRestroomUpsertResult =
        withContext(Dispatchers.IO) {
            upsertImportedRestroomInTx(ctx, createDto, matchKey)
        }

    suspend fun linkExternalMap(
        restroomId: UUID,
        provider: String,
        externalId: String
    ): RestroomResponseDto =
        withContext(Dispatchers.IO) {
            linkExternalMapInTx(ctx, restroomId, provider, externalId)
        }

    private fun computeIsOpen(
        restroomId: UUID?,
        workTimeJson: kotlinx.serialization.json.JsonObject?
    ): Boolean? {
        if (workTimeJson == null || workTimeJson.isEmpty() || scheduleMappingService == null) {
            return null
        }

        return try {
            val schedule = scheduleMappingService.mapSchedule(ImportProvider.TWO_GIS, workTimeJson)
            ScheduleUtils.isOpenNow(schedule)
        } catch (e: Exception) {
            logger.warn("Failed to parse schedule for restroom $restroomId", e)
            null
        }
    }

    fun findByOriginsInTx(
        txCtx: DSLContext,
        origins: Collection<ImportOriginKey>
    ): List<RestroomResponseDto> {
        if (origins.isEmpty()) {
            return emptyList()
        }

        val condition =
            origins
                .map { origin ->
                    RESTROOMS.ORIGIN_PROVIDER
                        .eq(origin.provider.name)
                        .and(RESTROOMS.ORIGIN_ID.eq(origin.originId))
                }.reduce(Condition::or)

        return txCtx
            .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
            .from(RESTROOMS)
            .where(RESTROOMS.IS_DELETED.isFalse.and(condition))
            .fetch()
            .map(RestroomMapper::mapFromRecord)
    }

    fun findByExternalMapsInTx(
        txCtx: DSLContext,
        provider: String,
        externalIds: Collection<String>
    ): List<RestroomResponseDto> {
        if (externalIds.isEmpty()) {
            return emptyList()
        }

        val providerExternalIdField =
            DSL.field(
                "({0} ->> {1})",
                SQLDataType.VARCHAR,
                RESTROOMS.EXTERNAL_MAPS,
                DSL.inline(provider)
            )

        return txCtx
            .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
            .from(RESTROOMS)
            .where(
                RESTROOMS.IS_DELETED.isFalse
                    .and(providerExternalIdField.`in`(externalIds))
            ).fetch()
            .map(RestroomMapper::mapFromRecord)
    }

    fun findByMatchKeysInTx(
        txCtx: DSLContext,
        matchKeys: Collection<String>
    ): List<RestroomResponseDto> {
        if (matchKeys.isEmpty()) {
            return emptyList()
        }

        return txCtx
            .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
            .from(RESTROOMS)
            .where(
                RESTROOMS.IS_DELETED.isFalse
                    .and(restroomMatchKeyField.`in`(matchKeys))
            ).fetch()
            .map(RestroomMapper::mapFromRecord)
    }

    fun upsertImportedRestroomInTx(
        txCtx: DSLContext,
        createDto: RestroomCreateDto,
        matchKey: String?
    ): ImportedRestroomUpsertResult {
        val id = UUID.randomUUID()
        val now = Instant.now()
        val insertedField = DSL.field("xmax = 0", SQLDataType.BOOLEAN).`as`("inserted")
        val providerKey = providerKey(createDto.originProvider)
        val providerExternalId = createDto.originId
        val externalMapsField: Field<JSONB?> =
            if (providerKey != null && providerExternalId != null) {
                mergeProviderJsonField(RESTROOMS.EXTERNAL_MAPS, providerKey, providerExternalId)
            } else {
                DSL.inline(createDto.externalMaps.toJSONB(), SQLDataType.JSONB)
            }

        val insertStep = buildInsertQuery(txCtx, createDto, id, now)
        val insertWithMatchKey =
            if (matchKey != null) {
                insertStep.set(restroomMatchKeyField, matchKey)
            } else {
                insertStep
            }
        val updateStep =
            insertWithMatchKey
                .onConflict(RESTROOMS.ORIGIN_PROVIDER, RESTROOMS.ORIGIN_ID)
                .doUpdate()
                .set(RESTROOMS.CITY_ID, createDto.cityId)
                .set(RESTROOMS.BUILDING_ID, createDto.buildingId)
                .set(RESTROOMS.SUBWAY_STATION_ID, createDto.subwayStationId)
                .set(RESTROOMS.NAME, createDto.name)
                .set(RESTROOMS.ADDRESS, createDto.address.takeIf { !it.isNullOrBlank() && it != "null" })
                .set(RESTROOMS.PHONES, createDto.phones.toJSONB())
                .set(RESTROOMS.WORK_TIME, createDto.workTime.toJSONB())
                .set(RESTROOMS.FEE_TYPE, createDto.feeType?.name)
                .set(RESTROOMS.GENDER_TYPE, createDto.genderType?.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, createDto.accessibilityType.name)
                .set(RESTROOMS.PLACE_TYPE, createDto.placeType.code)
                .set(RESTROOMS.COORDINATES, pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, RESTROOMS.COORDINATES))
                .set(RESTROOMS.DATA_SOURCE, createDto.dataSource.name)
                .set(RESTROOMS.STATUS, createDto.status.name)
                .set(RESTROOMS.AMENITIES, createDto.amenities.toJSONB())
                .set(RESTROOMS.EXTERNAL_MAPS, externalMapsField)
                .set(RESTROOMS.ACCESS_NOTE, createDto.accessNote)
                .set(RESTROOMS.DIRECTION_GUIDE, createDto.directionGuide)
                .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, createDto.inheritBuildingSchedule)
                .set(RESTROOMS.HAS_PHOTOS, createDto.hasPhotos)
                .set(RESTROOMS.LOCATION_TYPE, createDto.locationType.name)
                .set(RESTROOMS.IS_HIDDEN, createDto.isHidden)
                .set(RESTROOMS.UPDATED_AT, now)
        val record =
            (
                if (matchKey != null) {
                    updateStep.set(restroomMatchKeyField, matchKey)
                } else {
                    updateStep
                }
            ).returningResult(*((getAllRestroomsFieldsWithCoordinates()) + insertedField).toTypedArray())
                .fetchOne()
                ?: throw EntityNotFoundException("Туалет", "не удалось сохранить")

        val restroom = RestroomMapper.mapFromRecord(record)
        val created = record.get("inserted", Boolean::class.java) ?: false
        return ImportedRestroomUpsertResult(restroom = restroom, created = created)
    }

    fun linkExternalMapInTx(
        txCtx: DSLContext,
        restroomId: UUID,
        provider: String,
        externalId: String
    ): RestroomResponseDto {
        txCtx
            .update(RESTROOMS)
            .set(RESTROOMS.EXTERNAL_MAPS, mergeProviderJsonField(RESTROOMS.EXTERNAL_MAPS, provider, externalId))
            .set(RESTROOMS.UPDATED_AT, Instant.now())
            .where(RESTROOMS.ID.eq(restroomId).and(RESTROOMS.IS_DELETED.isFalse))
            .execute()

        return txCtx
            .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
            .from(RESTROOMS)
            .where(RESTROOMS.ID.eq(restroomId).and(RESTROOMS.IS_DELETED.isFalse))
            .fetchOne()
            ?.let(RestroomMapper::mapFromRecord)
            ?: throw EntityNotFoundException("Туалет", restroomId.toString())
    }

    fun findByIdInTx(
        txCtx: DSLContext,
        restroomId: UUID
    ): RestroomResponseDto? =
        txCtx
            .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
            .from(RESTROOMS)
            .where(RESTROOMS.ID.eq(restroomId).and(RESTROOMS.IS_DELETED.isFalse))
            .fetchOne()
            ?.let(RestroomMapper::mapFromRecord)

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

    private fun providerKey(provider: ImportProvider): String? =
        when (provider) {
            ImportProvider.TWO_GIS -> "2gis"
            ImportProvider.YANDEX_MAPS -> "yandex"
            ImportProvider.GOOGLE_MAPS -> "google"
            ImportProvider.OSM -> "osm"
            ImportProvider.USER,
            ImportProvider.MANUAL -> null
        }
}
