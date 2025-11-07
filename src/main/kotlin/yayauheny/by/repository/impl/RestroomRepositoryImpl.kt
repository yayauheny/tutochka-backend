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
import yayauheny.by.di.RestroomRepo
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.stDWithin
import yayauheny.by.util.stDistance

class RestroomRepositoryImpl(
    private val ctx: DSLContext
) : RestroomRepo {
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

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val baseQuery =
                ctx
                    .selectFrom(RESTROOMS)
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
                    .selectFrom(RESTROOMS)
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
                .selectFrom(RESTROOMS)
                .where(
                    RESTROOMS.ID
                        .eq(id)
                        .and(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
                )
                .fetchOne()
                ?.map { RestroomMapper.mapFromRecord(it) }
        }

    override suspend fun save(createDto: RestroomCreateDto): RestroomResponseDto =
        withContext(Dispatchers.IO) {
            val record = RestroomMapper.mapToSaveRecord(ctx, createDto)
            val inserted = record.insert()

            if (inserted == 0) {
                throw IllegalStateException("Error during save restroom")
            }

            RestroomMapper.mapFromRecord(record)
        }

    override suspend fun update(
        id: UUID,
        updateDto: RestroomUpdateDto
    ): RestroomResponseDto =
        withContext(Dispatchers.IO) {
            val query = ctx.update(RESTROOMS)
            val updateStep = RestroomMapper.applyUpdateDto(query, updateDto)
            val updated =
                updateStep
                    .set(RESTROOMS.UPDATED_AT, Instant.now())
                    .where(RESTROOMS.ID.eq(id))
                    .returning()
                    .fetchOne()
                    ?: throw IllegalStateException("Failed to update restroom with id: $id")

            RestroomMapper.mapFromRecord(updated)
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

    suspend fun findNearestByLocation(
        latitude: Double,
        longitude: Double,
        limit: Int = 5,
        distanceMeters: Double = 1000.0
    ): List<NearestRestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val distanceField = stDistance(RESTROOMS.COORDINATES, latitude, longitude)

            ctx
                .select(RESTROOMS.asterisk(), distanceField.`as`("distance"))
                .from(RESTROOMS)
                .where(
                    stDWithin(RESTROOMS.COORDINATES, latitude, longitude, distanceMeters)
                        .and(RESTROOMS.STATUS.eq(RestroomStatus.ACTIVE.name))
                        .and(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
                )
                .orderBy(distanceField.asc())
                .limit(limit)
                .fetch()
                .map {
                    val distance = it.get("distance", Double::class.java)
                    RestroomMapper.mapToNearestRestroom(it, distance)
                }
        }

    suspend fun findByCityId(
        cityId: UUID,
        pagination: PaginationRequest
    ): PageResponse<RestroomResponseDto> =
        withContext(Dispatchers.IO) {
            val cityFilter = FilterCriteria("cityId", FilterOperator.EQ, cityId.toString())
            val filters = pagination.filters + cityFilter
            val baseQuery =
                ctx
                    .selectFrom(RESTROOMS)
                    .where(RESTROOMS.IS_DELETED.eq(false).or(RESTROOMS.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = restroomQueryBuilder
            )
        }
}
