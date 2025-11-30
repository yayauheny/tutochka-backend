package yayauheny.by.common.mapper

import java.time.Instant
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.LatLon
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.reqDouble
import yayauheny.by.util.toJSONBOrEmpty
import yayauheny.by.util.toJsonObjectOrEmpty

object RestroomMapper {
    fun mapFromRecord(record: Record): RestroomResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
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
            coordinates = LatLon(lat = lat, lon = lon),
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

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: RestroomUpdateDto
    ): UpdateSetMoreStep<*> {
        var q: UpdateSetMoreStep<*> =
            updateStep
                .set(RESTROOMS.ADDRESS, dto.address)
                .set(RESTROOMS.INHERIT_PARENT_SCHEDULE, dto.inheritParentSchedule)
                .set(RESTROOMS.FEE_TYPE, dto.feeType.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, dto.accessibilityType.name)
                .set(RESTROOMS.STATUS, dto.status.name)
                .set(RESTROOMS.UPDATED_AT, Instant.now())

        dto.name?.let { q = q.set(RESTROOMS.NAME, it) }
        dto.description?.let { q = q.set(RESTROOMS.DESCRIPTION, it) }
        dto.phones?.let { q = q.set(RESTROOMS.PHONES, it.toJSONBOrEmpty()) }
        dto.workTime?.let { q = q.set(RESTROOMS.WORK_TIME, it.toJSONBOrEmpty()) }
        dto.amenities?.let { q = q.set(RESTROOMS.AMENITIES, it.toJSONBOrEmpty()) }
        dto.parentPlaceName?.let { q = q.set(RESTROOMS.PARENT_PLACE_NAME, it) }
        dto.parentPlaceType?.let { q = q.set(RESTROOMS.PARENT_PLACE_TYPE, it) }

        return q
    }

    fun mapToNearestRestroom(
        record: Record,
        distanceMeters: Double
    ): NearestRestroomResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        return NearestRestroomResponseDto(
            id = record[RESTROOMS.ID]!!,
            name = record[RESTROOMS.NAME],
            address = record[RESTROOMS.ADDRESS]!!,
            coordinates = LatLon(lat = lat, lon = lon),
            distanceMeters = distanceMeters,
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            isOpen = null
        )
    }
}
