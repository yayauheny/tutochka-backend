package yayauheny.by.common.mapper

import java.time.Instant
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import by.yayauheny.shared.dto.LatLon
import by.yayauheny.shared.enums.AccessibilityType
import by.yayauheny.shared.enums.DataSourceType
import by.yayauheny.shared.enums.FeeType
import by.yayauheny.shared.enums.PlaceType
import by.yayauheny.shared.enums.RestroomStatus
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
            buildingId = record[RESTROOMS.BUILDING_ID],
            subwayStationId = record[RESTROOMS.SUBWAY_STATION_ID],
            name = record[RESTROOMS.NAME],
            address = record[RESTROOMS.ADDRESS]!!,
            phones = record[RESTROOMS.PHONES].toJsonObjectOrEmpty(),
            workTime = record[RESTROOMS.WORK_TIME].toJsonObjectOrEmpty(),
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            accessibilityType = AccessibilityType.valueOf(record[RESTROOMS.ACCESSIBILITY_TYPE]!!),
            placeType = PlaceType.fromString(record[RESTROOMS.PLACE_TYPE]),
            coordinates = LatLon(lat = lat, lon = lon),
            dataSource = DataSourceType.valueOf(record[RESTROOMS.DATA_SOURCE]!!),
            status = RestroomStatus.valueOf(record[RESTROOMS.STATUS]!!),
            amenities = record[RESTROOMS.AMENITIES].toJsonObjectOrEmpty(),
            externalMaps = record[RESTROOMS.EXTERNAL_MAPS].toJsonObjectOrEmpty(),
            accessNote = record[RESTROOMS.ACCESS_NOTE],
            directionGuide = record[RESTROOMS.DIRECTION_GUIDE],
            inheritBuildingSchedule = record[RESTROOMS.INHERIT_BUILDING_SCHEDULE] ?: false,
            hasPhotos = record[RESTROOMS.HAS_PHOTOS] ?: false,
            createdAt = record[RESTROOMS.CREATED_AT]!!,
            updatedAt = record[RESTROOMS.UPDATED_AT]!!,
            distanceMeters = record["distance_meters"] as? Int
        )
    }

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: RestroomUpdateDto
    ): UpdateSetMoreStep<*> {
        var q: UpdateSetMoreStep<*> =
            updateStep
                .set(RESTROOMS.ADDRESS, dto.address)
                .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, dto.inheritBuildingSchedule)
                .set(RESTROOMS.FEE_TYPE, dto.feeType.name)
                .set(RESTROOMS.ACCESSIBILITY_TYPE, dto.accessibilityType.name)
                .set(RESTROOMS.PLACE_TYPE, dto.placeType?.id)
                .set(RESTROOMS.STATUS, dto.status.name)
                .set(RESTROOMS.BUILDING_ID, dto.buildingId)
                .set(RESTROOMS.SUBWAY_STATION_ID, dto.subwayStationId)
                .set(RESTROOMS.UPDATED_AT, Instant.now())

        dto.name?.let { q = q.set(RESTROOMS.NAME, it) }
        dto.phones?.let { q = q.set(RESTROOMS.PHONES, it.toJSONBOrEmpty()) }
        dto.workTime?.let { q = q.set(RESTROOMS.WORK_TIME, it.toJSONBOrEmpty()) }
        dto.amenities?.let { q = q.set(RESTROOMS.AMENITIES, it.toJSONBOrEmpty()) }
        dto.externalMaps?.let { q = q.set(RESTROOMS.EXTERNAL_MAPS, it.toJSONBOrEmpty()) }
        dto.accessNote?.let { q = q.set(RESTROOMS.ACCESS_NOTE, it) }
        dto.directionGuide?.let { q = q.set(RESTROOMS.DIRECTION_GUIDE, it) }
        q = q.set(RESTROOMS.HAS_PHOTOS, dto.hasPhotos)

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
