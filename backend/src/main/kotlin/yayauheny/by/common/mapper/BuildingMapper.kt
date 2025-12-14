package yayauheny.by.common.mapper

import java.time.Instant
import java.util.UUID
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.building.BuildingUpdateDto
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.util.reqDouble
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.toJsonObjectOrEmpty
import yayauheny.by.model.dto.LatLon
import yayauheny.by.model.enums.PlaceType
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType

object BuildingMapper {
    fun mapFromRecord(record: Record): BuildingResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        return BuildingResponseDto(
            id = record[BUILDINGS.ID]!!,
            cityId = record[BUILDINGS.CITY_ID]!!,
            name = record[BUILDINGS.NAME],
            address = record[BUILDINGS.ADDRESS]!!,
            buildingType = PlaceType.fromString(record[BUILDINGS.BUILDING_TYPE]),
            workTime = record[BUILDINGS.WORK_TIME].toJsonObjectOrEmpty(),
            coordinates = LatLon(lat = lat, lon = lon),
            externalIds = record[BUILDINGS.EXTERNAL_IDS].toJsonObjectOrEmpty(),
            isDeleted = record[BUILDINGS.IS_DELETED] ?: false,
            createdAt = record[BUILDINGS.CREATED_AT]!!,
            updatedAt = record[BUILDINGS.UPDATED_AT]!!
        )
    }

    fun mapToSaveRecord(
        ctx: org.jooq.DSLContext,
        dto: BuildingCreateDto,
        id: UUID,
        now: Instant
    ): org.jooq.InsertReturningStep<*> =
        ctx
            .insertInto(BUILDINGS)
            .set(BUILDINGS.ID, id)
            .set(BUILDINGS.CITY_ID, dto.cityId)
            .set(BUILDINGS.NAME, dto.name)
            .set(BUILDINGS.ADDRESS, dto.address.takeIf { it.isNotBlank() })
            .set(BUILDINGS.BUILDING_TYPE, dto.buildingType?.id)
            .set(BUILDINGS.WORK_TIME, dto.workTime.toJSONBOrEmpty())
            .set(
                BUILDINGS.COORDINATES,
                yayauheny.by.util.pointExpr(dto.coordinates.lon, dto.coordinates.lat, BUILDINGS.COORDINATES)
            ).set(BUILDINGS.EXTERNAL_IDS, dto.externalIds.toJSONBOrEmpty())
            .set(DSL.field("import_status", SQLDataType.VARCHAR(20)), dto.importStatus.name.lowercase())
            .set(BUILDINGS.CREATED_AT, now)
            .set(BUILDINGS.UPDATED_AT, now)

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: BuildingUpdateDto
    ): UpdateSetMoreStep<*> {
        var q: UpdateSetMoreStep<*> =
            updateStep
                .set(BUILDINGS.UPDATED_AT, Instant.now())

        dto.cityId?.let { q = q.set(BUILDINGS.CITY_ID, it) }
        dto.name?.let { q = q.set(BUILDINGS.NAME, it) }
        dto.address?.let { q = q.set(BUILDINGS.ADDRESS, it) }
        dto.buildingType?.let { q = q.set(BUILDINGS.BUILDING_TYPE, it.id) }
        dto.workTime?.let { q = q.set(BUILDINGS.WORK_TIME, it.toJSONBOrEmpty()) }
        dto.externalIds?.let { q = q.set(BUILDINGS.EXTERNAL_IDS, it.toJSONBOrEmpty()) }
        dto.coordinates?.let {
            q =
                q.set(
                    BUILDINGS.COORDINATES,
                    yayauheny.by.util.pointExpr(it.lon, it.lat, BUILDINGS.COORDINATES)
                )
        }
        dto.importStatus?.let {
            q = q.set(DSL.field("import_status", SQLDataType.VARCHAR(20)), it.name.lowercase())
        }

        return q
    }
}
