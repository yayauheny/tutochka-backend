package yayauheny.by.repository.impl

import java.time.Instant
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import yayauheny.by.common.errors.EntityNotFoundException
import yayauheny.by.common.mapper.SubwayMapper
import yayauheny.by.model.subway.SubwayLineCreateDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationCreateDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.repository.SubwayRepository
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.tables.references.SUBWAY_LINES
import yayauheny.by.tables.references.SUBWAY_STATIONS
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
import yayauheny.by.util.knnOrderTo
import yayauheny.by.util.latAlias
import yayauheny.by.util.lonAlias
import yayauheny.by.util.transactionSuspend

class SubwayRepositoryImpl(
    private val ctx: DSLContext
) : SubwayRepository {
    private fun lineProjection(): Array<org.jooq.SelectFieldOrAsterisk> =
        arrayOf(
            SUBWAY_LINES.ID,
            SUBWAY_LINES.CITY_ID,
            SUBWAY_LINES.NAME_RU,
            SUBWAY_LINES.NAME_EN,
            SUBWAY_LINES.NAME_LOCAL,
            SUBWAY_LINES.NAME_LOCAL_LANG,
            SUBWAY_LINES.SHORT_CODE,
            SUBWAY_LINES.HEX_COLOR,
            SUBWAY_LINES.IS_DELETED,
            SUBWAY_LINES.CREATED_AT
        )

    private fun stationProjection(): Array<org.jooq.SelectFieldOrAsterisk> =
        arrayOf(
            SUBWAY_STATIONS.ID,
            SUBWAY_STATIONS.SUBWAY_LINE_ID,
            SUBWAY_STATIONS.NAME_RU,
            SUBWAY_STATIONS.NAME_EN,
            SUBWAY_STATIONS.NAME_LOCAL,
            SUBWAY_STATIONS.NAME_LOCAL_LANG,
            SUBWAY_STATIONS.IS_TRANSFER,
            SUBWAY_STATIONS.EXTERNAL_IDS,
            SUBWAY_STATIONS.COORDINATES.latAlias(),
            SUBWAY_STATIONS.COORDINATES.lonAlias(),
            SUBWAY_STATIONS.IS_DELETED,
            SUBWAY_STATIONS.CREATED_AT
        )

    private fun notDeletedLineCondition() = SUBWAY_LINES.IS_DELETED.isFalse

    private fun notDeletedStationCondition() = SUBWAY_STATIONS.IS_DELETED.isFalse

    private fun fetchStationById(
        ctx: DSLContext,
        id: UUID
    ): SubwayStationResponseDto? =
        ctx
            .select(*stationProjection())
            .from(SUBWAY_STATIONS)
            .where(SUBWAY_STATIONS.ID.eq(id).and(notDeletedStationCondition()))
            .fetchOne()
            ?.let { SubwayMapper.mapStationFromRecord(it) }

    override suspend fun createLine(createDto: SubwayLineCreateDto): SubwayLineResponseDto =
        ctx.transactionSuspend { txCtx ->
            val id = UUID.randomUUID()
            val now = Instant.now()
            val rec =
                SubwayMapper
                    .mapLineToSaveRecord(txCtx, createDto, id, now)
                    .returning(*lineProjection())
                    .fetchOne()
                    ?: throw EntityNotFoundException("Линия метро", "не удалось сохранить")
            SubwayMapper.mapLineFromRecord(rec)
        }

    override suspend fun findLineById(id: UUID): SubwayLineResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*lineProjection())
                .from(SUBWAY_LINES)
                .where(SUBWAY_LINES.ID.eq(id).and(notDeletedLineCondition()))
                .fetchOne()
                ?.let { SubwayMapper.mapLineFromRecord(it) }
        }

    override suspend fun findAllLinesByCityId(cityId: UUID): List<SubwayLineResponseDto> =
        withContext(Dispatchers.IO) {
            ctx
                .select(*lineProjection())
                .from(SUBWAY_LINES)
                .where(SUBWAY_LINES.CITY_ID.eq(cityId).and(notDeletedLineCondition()))
                .fetch()
                .map { SubwayMapper.mapLineFromRecord(it) }
        }

    override suspend fun createStation(createDto: SubwayStationCreateDto): SubwayStationResponseDto =
        ctx.transactionSuspend { txCtx ->
            val id = UUID.randomUUID()
            val now = Instant.now()
            SubwayMapper
                .mapStationToSaveRecord(txCtx, createDto, id, now)
                .execute()
            fetchStationById(txCtx, id) ?: throw EntityNotFoundException("Станция метро", "не удалось сохранить")
        }

    override suspend fun findStationById(id: UUID): SubwayStationResponseDto? =
        withContext(Dispatchers.IO) {
            ctx
                .select(*stationProjection())
                .from(SUBWAY_STATIONS)
                .where(SUBWAY_STATIONS.ID.eq(id).and(notDeletedStationCondition()))
                .fetchOne()
                ?.let { SubwayMapper.mapStationFromRecord(it) }
        }

    override suspend fun findStationsByLineId(lineId: UUID): List<SubwayStationResponseDto> =
        withContext(Dispatchers.IO) {
            ctx
                .select(*stationProjection())
                .from(SUBWAY_STATIONS)
                .where(SUBWAY_STATIONS.SUBWAY_LINE_ID.eq(lineId).and(notDeletedStationCondition()))
                .orderBy(SUBWAY_STATIONS.NAME_RU.asc())
                .fetch()
                .map { SubwayMapper.mapStationFromRecord(it) }
        }

    override suspend fun findNearestStation(
        lat: Double,
        lon: Double
    ): SubwayStationResponseDto? =
        withContext(Dispatchers.IO) {
            val knnField = SUBWAY_STATIONS.COORDINATES.knnOrderTo(lat, lon)
            ctx
                .select(*stationProjection())
                .from(SUBWAY_STATIONS)
                .where(notDeletedStationCondition())
                .orderBy(knnField.asc())
                .limit(1)
                .fetchOne()
                ?.let { SubwayMapper.mapStationFromRecord(it) }
        }

    override suspend fun setNearestStationForRestroom(
        restroomId: UUID,
        lat: Double,
        lon: Double
    ): Boolean =
        withContext(Dispatchers.IO) {
            val nearestStationSelect =
                ctx
                    .select(SUBWAY_STATIONS.ID)
                    .from(SUBWAY_STATIONS)
                    .join(SUBWAY_LINES)
                    .on(SUBWAY_STATIONS.SUBWAY_LINE_ID.eq(SUBWAY_LINES.ID))
                    .where(
                        SUBWAY_LINES.CITY_ID
                            .eq(RESTROOMS.CITY_ID)
                            .and(SUBWAY_STATIONS.IS_DELETED.isFalse)
                            .and(SUBWAY_LINES.IS_DELETED.isFalse)
                    ).orderBy(SUBWAY_STATIONS.COORDINATES.knnOrderTo(lat, lon))
                    .limit(1)

            val nearestStationField = DSL.field(nearestStationSelect)

            val rowsUpdated =
                ctx
                    .update(RESTROOMS)
                    .set(RESTROOMS.SUBWAY_STATION_ID, nearestStationField)
                    .set(RESTROOMS.UPDATED_AT, Instant.now())
                    .where(
                        RESTROOMS.ID
                            .eq(restroomId)
                            .and(RESTROOMS.SUBWAY_STATION_ID.isDistinctFrom(nearestStationField))
                    ).execute()

            rowsUpdated > 0
        }

    override suspend fun batchUpdateStationsForCity(
        cityId: UUID,
        forceUpdate: Boolean
    ): Int =
        withContext(Dispatchers.IO) {
            val restroomsCityIdField = DSL.field("restrooms.city_id", SQLDataType.UUID)
            val restroomsCoordinatesField = DSL.field("restrooms.coordinates", SQLDataType.OTHER)

            val subquery =
                ctx
                    .select(SUBWAY_STATIONS.ID)
                    .from(SUBWAY_STATIONS)
                    .join(SUBWAY_LINES)
                    .on(SUBWAY_STATIONS.SUBWAY_LINE_ID.eq(SUBWAY_LINES.ID))
                    .where(
                        SUBWAY_LINES.CITY_ID
                            .eq(restroomsCityIdField)
                            .and(SUBWAY_STATIONS.IS_DELETED.isFalse)
                            .and(SUBWAY_LINES.IS_DELETED.isFalse)
                    ).orderBy(
                        DSL.field(
                            "{0} <-> {1}",
                            SQLDataType.DOUBLE,
                            SUBWAY_STATIONS.COORDINATES,
                            restroomsCoordinatesField
                        )
                    ).limit(1)
                    .asField<UUID>("nearest_id")

            val condition =
                if (forceUpdate) {
                    RESTROOMS.CITY_ID.eq(cityId)
                } else {
                    RESTROOMS.CITY_ID.eq(cityId).and(RESTROOMS.SUBWAY_STATION_ID.isNull)
                }

            ctx
                .update(RESTROOMS)
                .set(RESTROOMS.SUBWAY_STATION_ID, subquery)
                .set(RESTROOMS.UPDATED_AT, Instant.now())
                .where(condition)
                .execute()
        }
}
