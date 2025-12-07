package yayauheny.by.common.mapper

import java.time.Instant
import java.util.UUID
import org.jooq.Record
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
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
        val nameLocalField = DSL.field("name_local", SQLDataType.VARCHAR(255))
        val nameLocalLangField = DSL.field("name_local_lang", SQLDataType.VARCHAR(10))
        val shortCodeField = DSL.field("short_code", SQLDataType.VARCHAR(20))

        return SubwayLineResponseDto(
            id = record[SUBWAY_LINES.ID]!!,
            cityId = record[SUBWAY_LINES.CITY_ID]!!,
            nameRu = record[SUBWAY_LINES.NAME_RU]!!,
            nameEn = record[SUBWAY_LINES.NAME_EN]!!,
            nameLocal = record[nameLocalField] as? String,
            nameLocalLang = record[nameLocalLangField] as? String,
            shortCode = record[shortCodeField] as? String,
            hexColor = record[SUBWAY_LINES.HEX_COLOR]!!,
            isDeleted = record[SUBWAY_LINES.IS_DELETED] ?: false,
            createdAt = record[SUBWAY_LINES.CREATED_AT]!!
        )
    }

    fun mapStationFromRecord(record: Record): SubwayStationResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        val nameLocalField = DSL.field("name_local", SQLDataType.VARCHAR(255))
        val nameLocalLangField = DSL.field("name_local_lang", SQLDataType.VARCHAR(10))
        val isTransferField = DSL.field("is_transfer", SQLDataType.BOOLEAN)
        val externalIdsField = DSL.field("external_ids", org.jooq.impl.SQLDataType.JSONB)

        return SubwayStationResponseDto(
            id = record[SUBWAY_STATIONS.ID]!!,
            subwayLineId = record[SUBWAY_STATIONS.SUBWAY_LINE_ID]!!,
            nameRu = record[SUBWAY_STATIONS.NAME_RU]!!,
            nameEn = record[SUBWAY_STATIONS.NAME_EN]!!,
            nameLocal = record[nameLocalField] as? String,
            nameLocalLang = record[nameLocalLangField] as? String,
            isTransfer = record[isTransferField] as? Boolean ?: false,
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
    ): org.jooq.InsertReturningStep<*> {
        val nameLocalField = DSL.field("name_local", SQLDataType.VARCHAR(255))
        val nameLocalLangField = DSL.field("name_local_lang", SQLDataType.VARCHAR(10))
        val shortCodeField = DSL.field("short_code", SQLDataType.VARCHAR(20))

        return ctx
            .insertInto(SUBWAY_LINES)
            .set(SUBWAY_LINES.ID, id)
            .set(SUBWAY_LINES.CITY_ID, dto.cityId)
            .set(SUBWAY_LINES.NAME_RU, dto.nameRu)
            .set(SUBWAY_LINES.NAME_EN, dto.nameEn)
            .set(nameLocalField, dto.nameLocal)
            .set(nameLocalLangField, dto.nameLocalLang)
            .set(shortCodeField, dto.shortCode)
            .set(SUBWAY_LINES.HEX_COLOR, dto.hexColor)
            .set(SUBWAY_LINES.CREATED_AT, now)
    }

    fun mapStationToSaveRecord(
        ctx: org.jooq.DSLContext,
        dto: SubwayStationCreateDto,
        id: UUID,
        now: Instant
    ): org.jooq.InsertReturningStep<*> {
        val nameLocalField = DSL.field("name_local", SQLDataType.VARCHAR(255))
        val nameLocalLangField = DSL.field("name_local_lang", SQLDataType.VARCHAR(10))
        val isTransferField = DSL.field("is_transfer", SQLDataType.BOOLEAN)
        val externalIdsField = DSL.field("external_ids", org.jooq.impl.SQLDataType.JSONB)

        return ctx
            .insertInto(SUBWAY_STATIONS)
            .set(SUBWAY_STATIONS.ID, id)
            .set(SUBWAY_STATIONS.SUBWAY_LINE_ID, dto.subwayLineId)
            .set(SUBWAY_STATIONS.NAME_RU, dto.nameRu)
            .set(SUBWAY_STATIONS.NAME_EN, dto.nameEn)
            .set(nameLocalField, dto.nameLocal)
            .set(nameLocalLangField, dto.nameLocalLang)
            .set(isTransferField, dto.isTransfer)
            .set(externalIdsField, dto.externalIds?.let { org.jooq.JSONB.jsonb(it.toString()) } ?: org.jooq.JSONB.jsonb("{}"))
            .set(SUBWAY_STATIONS.COORDINATES, pointExpr(dto.coordinates.lon, dto.coordinates.lat, SUBWAY_STATIONS.COORDINATES))
            .set(SUBWAY_STATIONS.CREATED_AT, now)
    }
}
