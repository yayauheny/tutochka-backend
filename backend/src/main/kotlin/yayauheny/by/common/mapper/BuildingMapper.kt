package yayauheny.by.common.mapper

import java.time.Instant
import java.util.UUID
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.building.BuildingCreateDto
import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.building.BuildingUpdateDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.tables.references.BUILDINGS
import yayauheny.by.util.pointExpr
import yayauheny.by.util.reqDouble
import yayauheny.by.util.setIfNotNull
import yayauheny.by.util.setIfNotNullCoordinates
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.toJsonObjectOrEmpty

object BuildingMapper {
    fun mapFromRecord(record: Record): BuildingResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        return BuildingResponseDto(
            id = record[BUILDINGS.ID]!!,
            cityId = record[BUILDINGS.CITY_ID]!!,
            name = record[BUILDINGS.NAME],
            address = record[BUILDINGS.ADDRESS]!!,
            buildingType = PlaceType.fromCode(record[BUILDINGS.BUILDING_TYPE]),
            workTime = record[BUILDINGS.WORK_TIME].toJsonObjectOrEmpty(),
            coordinates = Coordinates(lat = lat, lon = lon),
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
            .set(BUILDINGS.BUILDING_TYPE, dto.buildingType?.code)
            .set(BUILDINGS.WORK_TIME, dto.workTime.toJSONBOrEmpty())
            .set(
                BUILDINGS.COORDINATES,
                pointExpr(dto.coordinates.lon, dto.coordinates.lat, BUILDINGS.COORDINATES)
            ).set(BUILDINGS.EXTERNAL_IDS, dto.externalIds.toJSONBOrEmpty())
            .set(BUILDINGS.IMPORT_STATUS, dto.importStatus.name)
            .set(BUILDINGS.CREATED_AT, now)
            .set(BUILDINGS.UPDATED_AT, now)

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: BuildingUpdateDto
    ): UpdateSetMoreStep<*> {
        val now = Instant.now()
        return updateStep
            .set(BUILDINGS.UPDATED_AT, now)
            .setIfNotNull(BUILDINGS.CITY_ID, dto.cityId)
            .setIfNotNull(BUILDINGS.NAME, dto.name)
            .setIfNotNull(BUILDINGS.ADDRESS, dto.address)
            .setIfNotNull(BUILDINGS.BUILDING_TYPE, dto.buildingType?.code)
            .setIfNotNull(BUILDINGS.WORK_TIME, dto.workTime?.toJSONBOrEmpty())
            .setIfNotNull(BUILDINGS.EXTERNAL_IDS, dto.externalIds?.toJSONBOrEmpty())
            .setIfNotNullCoordinates(BUILDINGS.COORDINATES, dto.coordinates)
            .setIfNotNull(BUILDINGS.IMPORT_STATUS, dto.importStatus?.name)
    }
}
