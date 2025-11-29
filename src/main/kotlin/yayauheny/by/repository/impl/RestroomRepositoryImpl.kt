package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.common.mapper.RestroomMapper
import yayauheny.by.common.query.FieldMeta
import yayauheny.by.common.query.FieldParsers
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PageResponse
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.builder.QueryBuilder
import yayauheny.by.common.query.builder.QueryExecutor
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.repository.RestroomRepository
import yayauheny.by.tables.references.RESTROOMS
import org.jooq.SelectFieldOrAsterisk
import yayauheny.by.util.distanceGeographyTo
import yayauheny.by.util.knnOrderTo
import yayauheny.by.util.pointExpr
import yayauheny.by.util.reqDouble
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.withinDistanceOf

class RestroomRepositoryImpl(
    private val ctx: DSLContext
) : RestroomRepository {
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
     * Единый набор полей для SELECT/RETURNING запросов туалетов.
     * Включает все поля таблицы + lat/lon координаты (реальные колонки).
     */
    private fun restroomProjection(): Array<SelectFieldOrAsterisk> {
        val r = RESTROOMS
        return arrayOf(
            r.ID,
            r.CITY_ID,
            r.NAME,
            r.DESCRIPTION,
            r.ADDRESS,
            r.PHONES,
            r.WORK_TIME,
            r.FEE_TYPE,
            r.ACCESSIBILITY_TYPE,
            r.DATA_SOURCE,
            r.STATUS,
            r.AMENITIES,
            r.PARENT_PLACE_NAME,
            r.PARENT_PLACE_TYPE,
            r.INHERIT_PARENT_SCHEDULE,
            r.IS_DELETED,
            r.CREATED_AT,
            r.UPDATED_AT,
            r.DELETED_AT,
            r.LAT,
            r.LON
        )
    }

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*restroomProjection())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination,
                builder = restroomQueryBuilder
            )
        }

    override suspend fun findSingle(filters: List<FilterCriteria>): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .select(*restroomProjection())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
            executor.executeSingle(
                baseQuery = baseQuery,
                filters = filters,
                builder = restroomQueryBuilder
            )
        }

    override suspend fun findById(id: UUID): RestroomResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*restroomProjection())
                .from(RESTROOMS)
                .where(
                    RESTROOMS.ID
                        .eq(id)
                        .and(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
                ).fetchOne()
                ?.map { RestroomMapper.mapFromRecord(it) }
        }

    override suspend fun save(createDto: RestroomCreateDto): RestroomResponseDto =
        withContext(Dispatchers.IO) {
            val r = RESTROOMS
            val id = UUID.randomUUID()
            val now = Instant.now()

            val rec =
                ctx
                    .insertInto(r)
                    .set(r.ID, id)
                    .set(r.CITY_ID, createDto.cityId)
                    .set(r.NAME, createDto.name)
                    .set(r.DESCRIPTION, createDto.description)
                    .set(r.ADDRESS, createDto.address)
                    .set(r.PHONES, createDto.phones.toJSONBOrEmpty())
                    .set(r.WORK_TIME, createDto.workTime.toJSONBOrEmpty())
                    .set(r.FEE_TYPE, createDto.feeType.name)
                    .set(r.ACCESSIBILITY_TYPE, createDto.accessibilityType.name)
                    .set(
                        r.COORDINATES,
                        pointExpr(createDto.coordinates.lon, createDto.coordinates.lat, r.COORDINATES)
                    ).set(r.DATA_SOURCE, createDto.dataSource.name)
                    .set(r.STATUS, createDto.status.name)
                    .set(r.AMENITIES, createDto.amenities.toJSONBOrEmpty())
                    .set(r.PARENT_PLACE_NAME, createDto.parentPlaceName)
                    .set(r.PARENT_PLACE_TYPE, createDto.parentPlaceType)
                    .set(r.INHERIT_PARENT_SCHEDULE, createDto.inheritParentSchedule)
                    .set(r.CREATED_AT, now)
                    .set(r.UPDATED_AT, now)
                    .returning(*restroomProjection())
                    .fetchOne() ?: error("Error during save restroom")

            RestroomMapper.mapFromRecord(rec)
        }

    override suspend fun update(
        id: UUID,
        updateDto: RestroomUpdateDto
    ): RestroomResponseDto =
        withContext(Dispatchers.IO) {
            val r = RESTROOMS
            val query = ctx.update(r)
            val updateStep = RestroomMapper.applyUpdateDto(query, updateDto)
            val rec =
                updateStep
                    .set(
                        r.COORDINATES,
                        pointExpr(updateDto.coordinates.lon, updateDto.coordinates.lat, r.COORDINATES)
                    ).set(r.UPDATED_AT, Instant.now())
                    .where(r.ID.eq(id))
                    .returning(*restroomProjection())
                    .fetchOne() ?: error("Failed to update restroom $id")

            RestroomMapper.mapFromRecord(rec)
        }

    override suspend fun deleteById(id: UUID): Boolean =
        withContext(Dispatchers.IO) {
            ctx
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
    ): List<NearestRestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val maxDistance = (distanceMeters ?: 1000).toDouble()
            val knnField = RESTROOMS.COORDINATES.knnOrderTo(latitude, longitude)
            val distanceField = RESTROOMS.COORDINATES.distanceGeographyTo(latitude, longitude)

            ctx
                .select(*restroomProjection(), distanceField.`as`("distance"))
                .from(RESTROOMS)
                .where(
                    RESTROOMS.COORDINATES
                        .withinDistanceOf(latitude, longitude, maxDistance)
                        .and(RESTROOMS.STATUS.eq(RestroomStatus.ACTIVE.name))
                        .and(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
                ).orderBy(knnField.asc())
                .limit(limit ?: 5)
                .fetch()
                .map {
                    val distance = it.reqDouble("distance")
                    RestroomMapper.mapToNearestRestroom(it, distance)
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
                    .select(*restroomProjection())
                    .from(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = restroomQueryBuilder
            )
        }
}
