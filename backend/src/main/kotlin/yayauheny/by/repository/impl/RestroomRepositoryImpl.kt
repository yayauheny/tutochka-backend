package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL
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
import by.yayauheny.shared.enums.RestroomStatus
import by.yayauheny.shared.dto.NearestRestroomSlimDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.tables.references.SUBWAY_STATIONS
import yayauheny.by.tables.references.SUBWAY_LINES
import yayauheny.by.util.distanceGeographyTo
import yayauheny.by.util.knnOrderTo
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.pointExpr
import yayauheny.by.util.reqDouble
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.transactionSuspend
import yayauheny.by.util.withinDistanceOf
import yayauheny.by.config.ApiConstants
import yayauheny.by.service.import.schedule.ScheduleMappingService
import yayauheny.by.model.import.ImportProvider
import yayauheny.by.model.schedule.ScheduleUtils
import org.slf4j.LoggerFactory

class RestroomRepositoryImpl(
    private val ctx: DSLContext,
    private val scheduleMappingService: ScheduleMappingService? = null
) : RestroomRepository {
    private val logger = LoggerFactory.getLogger(RestroomRepositoryImpl::class.java)
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
    private fun getAllRestroomsFieldsExceptCoordinates(): List<org.jooq.Field<*>> {
        val r = RESTROOMS
        return listOf(
            r.ID,
            r.CITY_ID,
            r.BUILDING_ID,
            r.SUBWAY_STATION_ID,
            r.NAME,
            r.ADDRESS,
            r.PHONES,
            r.WORK_TIME,
            r.FEE_TYPE,
            r.ACCESSIBILITY_TYPE,
            r.PLACE_TYPE,
            r.DATA_SOURCE,
            r.STATUS,
            r.AMENITIES,
            r.EXTERNAL_MAPS,
            r.ACCESS_NOTE,
            r.DIRECTION_GUIDE,
            r.INHERIT_BUILDING_SCHEDULE,
            r.HAS_PHOTOS,
            r.IS_DELETED,
            r.CREATED_AT,
            r.UPDATED_AT,
            r.DELETED_AT
        )
    }

    /**
     * Возвращает все поля таблицы RESTROOMS с координатами (lat/lon) для SELECT запросов.
     * Устраняет дублирование кода создания latField и lonField в репозиториях.
     */
    private fun getAllRestroomsFieldsWithCoordinates(): List<org.jooq.Field<*>> {
        val r = RESTROOMS
        return getAllRestroomsFieldsExceptCoordinates() + r.COORDINATES.latAlias() + r.COORDINATES.lonAlias()
    }

    /**
     * Возвращает координатные поля (lat/lon) для таблицы RESTROOMS.
     * Используется в запросах, где нужны только координаты без других полей.
     */
    private fun getRestroomsCoordinateFields(): List<org.jooq.Field<*>> {
        val r = RESTROOMS
        return listOf(r.COORDINATES.latAlias(), r.COORDINATES.lonAlias())
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
            val b = BUILDINGS
            val s = SUBWAY_STATIONS
            val l = SUBWAY_LINES

            // Enriched query: include building and subway station data
            val selectFields =
                getAllRestroomsFieldsWithCoordinates() +
                    listOf(
                        // Building fields
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
                        // Subway station fields
                        s.ID.`as`("s_id"),
                        s.NAME_RU.`as`("s_name_ru"),
                        s.NAME_EN.`as`("s_name_en"),
                        s.NAME_LOCAL.`as`("s_name_local"),
                        s.NAME_LOCAL_LANG.`as`("s_name_local_lang"),
                        s.IS_TRANSFER.`as`("s_is_transfer"),
                        s.COORDINATES.latAlias().`as`("s_lat"),
                        s.COORDINATES.lonAlias().`as`("s_lon"),
                        s.IS_DELETED.`as`("s_is_deleted"),
                        s.CREATED_AT.`as`("s_created_at"),
                        // Subway line fields
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
                    // Use enriched mapper that includes building and subway
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
        .set(RESTROOMS.ADDRESS, createDto.address)
        .set(RESTROOMS.PHONES, createDto.phones.toJSONBOrEmpty())
        .set(RESTROOMS.WORK_TIME, createDto.workTime.toJSONBOrEmpty())
        .set(RESTROOMS.FEE_TYPE, createDto.feeType.name)
        .set(RESTROOMS.ACCESSIBILITY_TYPE, createDto.accessibilityType.name)
        .set(RESTROOMS.PLACE_TYPE, createDto.placeType.id)
        .set(
            RESTROOMS.COORDINATES,
            pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, RESTROOMS.COORDINATES)
        ).set(RESTROOMS.DATA_SOURCE, createDto.dataSource.name)
        .set(RESTROOMS.STATUS, createDto.status.name)
        .set(RESTROOMS.AMENITIES, createDto.amenities.toJSONBOrEmpty())
        .set(RESTROOMS.EXTERNAL_MAPS, createDto.externalMaps.toJSONBOrEmpty())
        .set(RESTROOMS.ACCESS_NOTE, createDto.accessNote)
        .set(RESTROOMS.DIRECTION_GUIDE, createDto.directionGuide)
        .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, createDto.inheritBuildingSchedule)
        .set(RESTROOMS.HAS_PHOTOS, createDto.hasPhotos)
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
        .set(
            RESTROOMS.COORDINATES,
            pointExpr(updateDto.coordinates.lon, updateDto.coordinates.lat, RESTROOMS.COORDINATES)
        ).set(RESTROOMS.UPDATED_AT, Instant.now())
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
        latitude: Double,
        longitude: Double,
        limit: Int?,
        distanceMeters: Int?
    ): List<NearestRestroomSlimDto> =
        withContext(Dispatchers.IO) {
            val maxDistance = (distanceMeters ?: ApiConstants.DEFAULT_MAX_DISTANCE_METERS).toDouble()
            val coordinateFields = getRestroomsCoordinateFields()
            val knnField = RESTROOMS.COORDINATES.knnOrderTo(latitude, longitude)
            val distanceField = RESTROOMS.COORDINATES.distanceGeographyTo(latitude, longitude)
            val b = BUILDINGS
            val s = SUBWAY_STATIONS
            val l = SUBWAY_LINES

            // Slim query: only fields needed for list display
            // Restroom: id, name, fee_type, coordinates
            // Building: name, address (for displayName fallback)
            // Subway: name_ru, name_en, name_local, name_local_lang, line hex_color
            val selectFields =
                listOf(
                    RESTROOMS.ID,
                    RESTROOMS.NAME,
                    RESTROOMS.FEE_TYPE
                ) +
                    coordinateFields +
                    distanceField.`as`("distance") +
                    listOf(
                        // Building fields for displayName fallback
                        b.NAME.`as`("b_name"),
                        b.ADDRESS.`as`("b_address"),
                        // Subway station fields for list display
                        s.ID.`as`("s_id"),
                        s.NAME_RU.`as`("s_name_ru"),
                        s.NAME_EN.`as`("s_name_en"),
                        s.NAME_LOCAL.`as`("s_name_local"),
                        s.NAME_LOCAL_LANG.`as`("s_name_local_lang"),
                        // Subway line color for emoji
                        l.HEX_COLOR.`as`("l_hex")
                    )

            ctx
                .select(*selectFields.toTypedArray())
                .from(RESTROOMS)
                .leftJoin(b)
                .on(RESTROOMS.BUILDING_ID.eq(b.ID).and(b.IS_DELETED.isFalse))
                .leftJoin(s)
                .on(RESTROOMS.SUBWAY_STATION_ID.eq(s.ID).and(s.IS_DELETED.isFalse))
                .leftJoin(l)
                .on(s.SUBWAY_LINE_ID.eq(l.ID).and(l.IS_DELETED.isFalse))
                .where(
                    RESTROOMS.COORDINATES
                        .withinDistanceOf(latitude, longitude, maxDistance)
                        .and(RESTROOMS.STATUS.eq(RestroomStatus.ACTIVE.name))
                        .and(RESTROOMS.IS_DELETED.isFalse)
                ).orderBy(knnField.asc())
                .limit(limit ?: 5)
                .fetch()
                .map { record ->
                    val distance = record.reqDouble("distance")
                    RestroomMapper.mapToNearestRestroomSlim(record, distance)
                }
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

    override suspend fun findByExternalMap(
        provider: String,
        externalId: String
    ): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*getAllRestroomsFieldsWithCoordinates().toTypedArray())
                .from(RESTROOMS)
                .where(
                    RESTROOMS.IS_DELETED.isFalse
                        .and(
                            DSL.condition(
                                "{0} @> jsonb_build_object({1}, {2})",
                                RESTROOMS.EXTERNAL_MAPS,
                                provider,
                                externalId
                            )
                        )
                ).orderBy(RESTROOMS.CREATED_AT.desc(), RESTROOMS.ID.desc())
                .limit(1)
                .fetchOne()
                ?.let { RestroomMapper.mapFromRecord(it) }
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
}
