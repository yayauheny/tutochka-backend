package yayauheny.by.common.mapper

import java.time.Instant
import java.util.UUID
import org.jooq.Record
import yayauheny.by.model.subway.SubwayLineCreateDto
import yayauheny.by.model.subway.SubwayLineResponseDto
import yayauheny.by.model.subway.SubwayStationCreateDto
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.tables.references.SUBWAY_LINES
import yayauheny.by.tables.references.SUBWAY_STATIONS
import yayauheny.by.util.pointExpr
import yayauheny.by.util.reqDouble
import by.yayauheny.shared.dto.LatLon

object SubwayMapper {
    fun mapLineFromRecord(record: Record): SubwayLineResponseDto {
        return SubwayLineResponseDto(
            id = record[SUBWAY_LINES.ID]!!,
            cityId = record[SUBWAY_LINES.CITY_ID]!!,
            nameRu = record[SUBWAY_LINES.NAME_RU]!!,
            nameEn = record[SUBWAY_LINES.NAME_EN]!!,
            hexColor = record[SUBWAY_LINES.HEX_COLOR]!!,
            isDeleted = record[SUBWAY_LINES.IS_DELETED] ?: false,
            createdAt = record[SUBWAY_LINES.CREATED_AT]!!
        )
    }

    fun mapStationFromRecord(record: Record): SubwayStationResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        return SubwayStationResponseDto(
            id = record[SUBWAY_STATIONS.ID]!!,
            subwayLineId = record[SUBWAY_STATIONS.SUBWAY_LINE_ID]!!,
            nameRu = record[SUBWAY_STATIONS.NAME_RU]!!,
            nameEn = record[SUBWAY_STATIONS.NAME_EN]!!,
            coordinates = LatLon(lat = lat, lon = lon),
            isDeleted = record[SUBWAY_STATIONS.IS_DELETED] ?: false,
            createdAt = record[SUBWAY_STATIONS.CREATED_AT]!!
        )
    }

    fun mapLineToSaveRecord(
        ctx: org.jooq.DSLContext,
        dto: SubwayLineCreateDto,
        id: UUID,
        now: Instant
    ): org.jooq.InsertReturningStep<*> =
        ctx
            .insertInto(SUBWAY_LINES)
            .set(SUBWAY_LINES.ID, id)
            .set(SUBWAY_LINES.CITY_ID, dto.cityId)
            .set(SUBWAY_LINES.NAME_RU, dto.nameRu)
            .set(SUBWAY_LINES.NAME_EN, dto.nameEn)
            .set(SUBWAY_LINES.HEX_COLOR, dto.hexColor)
            .set(SUBWAY_LINES.CREATED_AT, now)

    fun mapStationToSaveRecord(
        ctx: org.jooq.DSLContext,
        dto: SubwayStationCreateDto,
        id: UUID,
        now: Instant
    ): org.jooq.InsertReturningStep<*> =
        ctx
            .insertInto(SUBWAY_STATIONS)
            .set(SUBWAY_STATIONS.ID, id)
            .set(SUBWAY_STATIONS.SUBWAY_LINE_ID, dto.subwayLineId)
            .set(SUBWAY_STATIONS.NAME_RU, dto.nameRu)
            .set(SUBWAY_STATIONS.NAME_EN, dto.nameEn)
            .set(SUBWAY_STATIONS.COORDINATES, pointExpr(dto.coordinates.lon, dto.coordinates.lat, SUBWAY_STATIONS.COORDINATES))
            .set(SUBWAY_STATIONS.CREATED_AT, now)
}
