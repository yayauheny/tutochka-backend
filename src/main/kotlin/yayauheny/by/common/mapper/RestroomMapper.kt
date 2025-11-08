package yayauheny.by.common.mapper

import com.vividsolutions.jts.geom.Point
import java.time.Instant
import java.util.UUID
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.tables.records.RestroomsRecord
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.toJsonObjectOrEmpty
import yayauheny.by.util.toPoint

object RestroomMapper {
    fun mapFromRecord(record: Record): RestroomResponseDto {
        val coordinates = record[RESTROOMS.COORDINATES]!! as Point
        return RestroomResponseDto(
            id = record[RESTROOMS.ID]!!,
            cityId = record[RESTROOMS.CITY_ID],
            name = record[RESTROOMS.NAME],
            description = record[RESTROOMS.DESCRIPTION],
            address = record[RESTROOMS.ADDRESS]!!,
            phones = record[RESTROOMS.PHONES].toJsonObjectOrEmpty(),
            workTime = record[RESTROOMS.WORK_TIME].toJsonObjectOrEmpty(),
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            accessibilityType = AccessibilityType.valueOf(record[RESTROOMS.ACCESSIBILITY_TYPE]!!),
            lat = coordinates.y,
            lon = coordinates.x,
            dataSource = DataSourceType.valueOf(record[RESTROOMS.DATA_SOURCE]!!),
            status = RestroomStatus.valueOf(record[RESTROOMS.STATUS]!!),
            amenities = record[RESTROOMS.AMENITIES].toJsonObjectOrEmpty(),
            parentPlaceName = record[RESTROOMS.PARENT_PLACE_NAME],
            parentPlaceType = record[RESTROOMS.PARENT_PLACE_TYPE],
            inheritParentSchedule = record[RESTROOMS.INHERIT_PARENT_SCHEDULE] ?: false,
            createdAt = record[RESTROOMS.CREATED_AT]!!,
            updatedAt = record[RESTROOMS.UPDATED_AT]!!
        )
    }

    fun mapToRecord(dto: RestroomCreateDto): RestroomsRecord {
        return RestroomsRecord().apply {
            id = UUID.randomUUID()
            cityId = dto.cityId
            name = dto.name
            description = dto.description
            address = dto.address
            feeType = dto.feeType.name
            accessibilityType = dto.accessibilityType.name
            coordinates = (dto.lat to dto.lon).toPoint()
            dataSource = dto.dataSource.name
            status = RestroomStatus.ACTIVE.name
            createdAt = Instant.now()
            updatedAt = Instant.now()
        }
    }

    fun mapToSaveRecord(
        ctx: DSLContext,
        dto: RestroomCreateDto
    ): RestroomsRecord {
        val record = ctx.newRecord(RESTROOMS)
        val coordinates = (dto.lat to dto.lon).toPoint()
        record.cityId = dto.cityId
        record.name = dto.name
        record.description = dto.description
        record.address = dto.address
        record.phones = dto.phones.toJSONBOrEmpty()
        record.workTime = dto.workTime.toJSONBOrEmpty()
        record.feeType = dto.feeType.name
        record.accessibilityType = dto.accessibilityType.name
        record.coordinates = coordinates
        record.dataSource = dto.dataSource.name
        record.status = dto.status.name
        record.amenities = dto.amenities.toJSONBOrEmpty()
        record.parentPlaceName = dto.parentPlaceName
        record.parentPlaceType = dto.parentPlaceType
        record.inheritParentSchedule = dto.inheritParentSchedule
        return record
    }

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: RestroomUpdateDto
    ): UpdateSetMoreStep<*> {
        val coordinates = (dto.lat to dto.lon).toPoint()
        var query: UpdateSetMoreStep<*> =
            updateStep
                .set(RESTROOMS.ADDRESS, dto.address)
                .set(RESTROOMS.INHERIT_PARENT_SCHEDULE, dto.inheritParentSchedule)
                .set(RESTROOMS.FEE_TYPE, dto.feeType.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, dto.accessibilityType.name)
                .set(RESTROOMS.STATUS, dto.status.name)
                .set(RESTROOMS.COORDINATES, coordinates)
                .set(RESTROOMS.UPDATED_AT, Instant.now())

        dto.name?.let { query = query.set(RESTROOMS.NAME, it) }
        dto.description?.let { query = query.set(RESTROOMS.DESCRIPTION, it) }
        dto.phones?.let { query = query.set(RESTROOMS.PHONES, it.toJSONBOrEmpty()) }
        dto.workTime?.let { query = query.set(RESTROOMS.WORK_TIME, it.toJSONBOrEmpty()) }
        dto.amenities?.let { query = query.set(RESTROOMS.AMENITIES, it.toJSONBOrEmpty()) }
        dto.parentPlaceName?.let { query = query.set(RESTROOMS.PARENT_PLACE_NAME, it) }
        dto.parentPlaceType?.let { query = query.set(RESTROOMS.PARENT_PLACE_TYPE, it) }

        return query
    }

    @Deprecated("Unused method, kept for reference", ReplaceWith("mapFromRecord(record)"))
    private fun RestroomsRecord.toRestroomResponseDto(): RestroomResponseDto {
        val point = this[RESTROOMS.COORDINATES]!! as Point
        return RestroomResponseDto(
            id = get(RESTROOMS.ID)!!,
            cityId = get(RESTROOMS.CITY_ID),
            name = get(RESTROOMS.NAME),
            description = get(RESTROOMS.DESCRIPTION),
            address = get(RESTROOMS.ADDRESS)!!,
            phones = get(RESTROOMS.PHONES)?.toJsonObjectOrEmpty(),
            workTime = get(RESTROOMS.WORK_TIME)?.toJsonObjectOrEmpty(),
            feeType = FeeType.valueOf(get(RESTROOMS.FEE_TYPE) ?: "FREE"),
            accessibilityType = AccessibilityType.valueOf(get(RESTROOMS.ACCESSIBILITY_TYPE)!!),
            lat = point.y,
            lon = point.x,
            dataSource = DataSourceType.valueOf(get(RESTROOMS.DATA_SOURCE)!!),
            status = RestroomStatus.valueOf(get(RESTROOMS.STATUS)!!),
            amenities = get(RESTROOMS.AMENITIES)?.toJsonObjectOrEmpty(),
            parentPlaceName = get(RESTROOMS.PARENT_PLACE_NAME),
            parentPlaceType = get(RESTROOMS.PARENT_PLACE_TYPE),
            inheritParentSchedule = get(RESTROOMS.INHERIT_PARENT_SCHEDULE) ?: false,
            createdAt = get(RESTROOMS.CREATED_AT)!!,
            updatedAt = get(RESTROOMS.UPDATED_AT)!!
        )
    }

    fun mapToNearestRestroom(
        record: Record,
        distanceMeters: Double
    ): yayauheny.by.model.restroom.NearestRestroomResponseDto {
        val coordinates = record[RESTROOMS.COORDINATES]!! as Point
        return yayauheny.by.model.restroom.NearestRestroomResponseDto(
            id = record[RESTROOMS.ID]!!,
            name = record[RESTROOMS.NAME],
            address = record[RESTROOMS.ADDRESS]!!,
            lat = coordinates.y,
            lon = coordinates.x,
            distanceMeters = distanceMeters,
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            isOpen = null // Could be determined from work_time if needed
        )
    }
}
