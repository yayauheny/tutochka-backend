package yayauheny.by.repository.impl

import java.time.Instant
import java.util.*
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
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityResponseDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.repository.CityRepository
import yayauheny.by.tables.references.CITIES
import yayauheny.by.util.asGeoJson
import yayauheny.by.util.geomFromGeoJson
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.pointExpr

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

    override suspend fun findAll(pagination: PaginationRequest): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val latField = CITIES.COORDINATES.latAlias()
            val lonField = CITIES.COORDINATES.lonAlias()
            val cityBoundsJson = CITIES.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            val baseQuery =
                ctx
                    .select(CITIES.asterisk(), latField, lonField, cityBoundsJson)
                    .from(CITIES)
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
            val latField = CITIES.COORDINATES.latAlias()
            val lonField = CITIES.COORDINATES.lonAlias()
            val cityBoundsJson = CITIES.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            ctx
                .select(CITIES.asterisk(), latField, lonField, cityBoundsJson)
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
            val latField = CITIES.COORDINATES.latAlias()
            val lonField = CITIES.COORDINATES.lonAlias()
            val cityBoundsJson = CITIES.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            val baseQuery =
                ctx
                    .select(CITIES.asterisk(), latField, lonField, cityBoundsJson)
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
            val r = CITIES
            val latF = r.COORDINATES.latAlias()
            val lonF = r.COORDINATES.lonAlias()
            val boundsJson = r.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")
            val id = UUID.randomUUID()
            val now = Instant.now()

            val coordinatesExpr = pointExpr(dto.coordinates.lon, dto.coordinates.lat, r.COORDINATES)
            val cityBoundsExpr = dto.cityBounds?.let { geomFromGeoJson(it, r.CITY_BOUNDS) }

            var insert =
                ctx
                    .insertInto(r)
                    .set(r.ID, id)
                    .set(r.COUNTRY_ID, dto.countryId)
                    .set(r.NAME_RU, dto.nameRu)
                    .set(r.NAME_EN, dto.nameEn)
                    .set(r.REGION, dto.region)
                    .set(r.COORDINATES, coordinatesExpr)
                    .set(r.CREATED_AT, now)
                    .set(r.UPDATED_AT, now)

            if (cityBoundsExpr != null) {
                insert = insert.set(r.CITY_BOUNDS, cityBoundsExpr)
            }

            val rec =
                insert
                    .returning(r.asterisk(), latF, lonF, boundsJson)
                    .fetchOne()
                    ?: error("Error during save city: ${dto.nameEn}")

            CityMapper.mapFromRecord(rec)
        }

    override suspend fun update(
        id: UUID,
        updateDto: CityUpdateDto
    ): CityResponseDto =
        withContext(Dispatchers.IO) {
            val r = CITIES
            val latF = r.COORDINATES.latAlias()
            val lonF = r.COORDINATES.lonAlias()
            val boundsJson = r.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            val query = ctx.update(r)
            val updateStep = CityMapper.applyUpdateDto(query, updateDto)
            val rec =
                updateStep
                    .set(
                        r.COORDINATES,
                        pointExpr(updateDto.coordinates.lon, updateDto.coordinates.lat, r.COORDINATES)
                    ).set(r.UPDATED_AT, Instant.now())
                    .where(r.ID.eq(id))
                    .returning(r.asterisk(), latF, lonF, boundsJson)
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
            val latField = CITIES.COORDINATES.latAlias()
            val lonField = CITIES.COORDINATES.lonAlias()
            val cityBoundsJson = CITIES.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            val countryFilter = FilterCriteria("countryId", FilterOperator.EQ, countryId.toString())
            val filters = pagination.filters + countryFilter
            val baseQuery =
                ctx
                    .select(CITIES.asterisk(), latField, lonField, cityBoundsJson)
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }

    override suspend fun findByName(
        name: String,
        pagination: PaginationRequest
    ): PageResponse<CityResponseDto> =
        withContext(Dispatchers.IO) {
            val latField = CITIES.COORDINATES.latAlias()
            val lonField = CITIES.COORDINATES.lonAlias()
            val cityBoundsJson = CITIES.CITY_BOUNDS.asGeoJson().`as`("city_bounds_json")

            val nameFilter = FilterCriteria("nameRu", FilterOperator.ILIKE, "%$name%")
            val filters = pagination.filters + nameFilter
            val baseQuery =
                ctx
                    .select(CITIES.asterisk(), latField, lonField, cityBoundsJson)
                    .from(CITIES)
                    .where(CITIES.IS_DELETED.eq(false).or(CITIES.IS_DELETED.isNull))
            executor.executePaginated(
                baseQuery = baseQuery,
                request = pagination.copy(filters = filters),
                builder = cityQueryBuilder,
                fetchCount = true
            )
        }
}
