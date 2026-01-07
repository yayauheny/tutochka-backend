package yayauheny.by.common.mapper

import yayauheny.by.model.dto.BuildingResponseDto
import yayauheny.by.model.dto.LatLon
import yayauheny.by.model.dto.NearestRestroomSlimDto
import yayauheny.by.model.dto.SubwayLineResponseDto
import yayauheny.by.model.dto.SubwayStationResponseDto
import yayauheny.by.model.dto.SubwayStationSlimDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import java.time.Instant
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import org.jooq.impl.DSL
import org.jooq.impl.SQLDataType
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
        val genderTypeStr = record[DSL.field("gender_type", SQLDataType.VARCHAR(20))]?.toString() ?: "UNKNOWN"
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
            genderType = GenderType.valueOf(genderTypeStr),
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
            updatedAt = record[RESTROOMS.UPDATED_AT]!!
        )
    }

    /**
     * Maps an enriched database record (with building and subway station joins) to RestroomResponseDto.
     * Includes building and subwayStation objects when available.
     */
    fun mapFromRecordEnriched(record: Record): RestroomResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")

        val bId = record.get("b_id") as? java.util.UUID
        val building =
            bId?.let {
                val bLat = record.get("b_lat", Double::class.java)
                val bLon = record.get("b_lon", Double::class.java)
                val bTypeRaw = record.get("b_type", String::class.java)
                val bAddress: String = record.get("b_address", String::class.java) ?: ""
                val bType = PlaceType.fromString(bTypeRaw)

                BuildingResponseDto(
                    id = it,
                    cityId = record.get("b_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                    name = record.get("b_name", String::class.java),
                    address = bAddress,
                    buildingType = bType,
                    workTime = record.get("b_work_time", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    coordinates = if (bLat != null && bLon != null) LatLon(bLat, bLon) else LatLon(lat, lon),
                    externalIds = record.get("b_external_ids", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    isDeleted = record.get("b_is_deleted", Boolean::class.java) ?: false,
                    createdAt = record.get("b_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!,
                    updatedAt = record.get("b_updated_at", java.time.Instant::class.java) ?: record[RESTROOMS.UPDATED_AT]!!
                )
            }

        val stationId = record.get("s_id") as? java.util.UUID
        val station =
            stationId?.let {
                val sLat = record.get("s_lat", Double::class.java)
                val sLon = record.get("s_lon", Double::class.java)
                val lineId = record.get("l_id") as? java.util.UUID
                val line =
                    lineId?.let {
                        SubwayLineResponseDto(
                            id = it,
                            cityId = record.get("l_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                            nameRu = record.get("l_name_ru", String::class.java) ?: "",
                            nameEn = record.get("l_name_en", String::class.java) ?: "",
                            hexColor = record.get("l_hex", String::class.java) ?: "",
                            isDeleted = record.get("l_is_deleted", Boolean::class.java) ?: false,
                            createdAt = record.get("l_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!
                        )
                    }
                SubwayStationResponseDto(
                    id = stationId,
                    subwayLineId = lineId ?: stationId, // fallback just to keep non-null id
                    nameRu = record.get("s_name_ru", String::class.java) ?: "",
                    nameEn = record.get("s_name_en", String::class.java) ?: "",
                    nameLocal = record.get("s_name_local", String::class.java),
                    nameLocalLang = record.get("s_name_local_lang", String::class.java),
                    isTransfer = record.get("s_is_transfer", Boolean::class.java) ?: false,
                    coordinates = if (sLat != null && sLon != null) LatLon(sLat, sLon) else LatLon(lat, lon),
                    isDeleted = record.get("s_is_deleted", Boolean::class.java) ?: false,
                    createdAt = record.get("s_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!,
                    line = line
                )
            }

        val genderTypeStr = record[DSL.field("gender_type", SQLDataType.VARCHAR(20))]?.toString() ?: "UNKNOWN"
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
            genderType = GenderType.valueOf(genderTypeStr),
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
            building = building,
            subwayStation = station
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
                .set(RESTROOMS.PLACE_TYPE, dto.placeType?.id)
                .set(RESTROOMS.STATUS, dto.status.name)
                .set(RESTROOMS.BUILDING_ID, dto.buildingId)
                .set(RESTROOMS.SUBWAY_STATION_ID, dto.subwayStationId)
                .set(RESTROOMS.UPDATED_AT, Instant.now())

        dto.genderType?.let { q = q.set(DSL.field("gender_type", SQLDataType.VARCHAR(20)), it.name) }
        dto.accessibilityType?.let { q = q.set(RESTROOMS.ACCESSIBILITY_TYPE, it.name) }
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
        distanceMeters: Double,
        isOpen: Boolean? = null
    ): NearestRestroomResponseDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")

        val bId = record.get("b_id") as? java.util.UUID
        val building =
            bId?.let {
                val bLat = record.get("b_lat", Double::class.java)
                val bLon = record.get("b_lon", Double::class.java)
                val bTypeRaw = record.get("b_type", String::class.java)
                val bAddress: String = record.get("b_address", String::class.java) ?: ""
                val bType = PlaceType.fromString(bTypeRaw)

                BuildingResponseDto(
                    id = it,
                    cityId = record.get("b_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                    name = record.get("b_name", String::class.java),
                    address = bAddress,
                    buildingType = bType,
                    workTime = record.get("b_work_time", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    coordinates = if (bLat != null && bLon != null) LatLon(bLat, bLon) else LatLon(lat, lon),
                    externalIds = record.get("b_external_ids", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    isDeleted = record.get("b_is_deleted", Boolean::class.java) ?: false,
                    createdAt = record.get("b_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!,
                    updatedAt = record.get("b_updated_at", java.time.Instant::class.java) ?: record[RESTROOMS.UPDATED_AT]!!
                )
            }

        val stationId = record.get("s_id") as? java.util.UUID
        val station =
            stationId?.let {
                val sLat = record.get("s_lat", Double::class.java)
                val sLon = record.get("s_lon", Double::class.java)
                val lineId = record.get("l_id") as? java.util.UUID
                val line =
                    lineId?.let {
                        SubwayLineResponseDto(
                            id = it,
                            cityId = record.get("l_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                            nameRu = record.get("l_name_ru", String::class.java) ?: "",
                            nameEn = record.get("l_name_en", String::class.java) ?: "",
                            hexColor = record.get("l_hex", String::class.java) ?: "",
                            isDeleted = record.get("l_is_deleted", Boolean::class.java) ?: false,
                            createdAt = record.get("l_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!
                        )
                    }
                SubwayStationResponseDto(
                    id = stationId,
                    subwayLineId = lineId ?: stationId, // fallback just to keep non-null id
                    nameRu = record.get("s_name_ru", String::class.java) ?: "",
                    nameEn = record.get("s_name_en", String::class.java) ?: "",
                    nameLocal = record.get("s_name_local", String::class.java),
                    nameLocalLang = record.get("s_name_local_lang", String::class.java),
                    isTransfer = record.get("s_is_transfer", Boolean::class.java) ?: false,
                    coordinates = if (sLat != null && sLon != null) LatLon(sLat, sLon) else LatLon(lat, lon),
                    isDeleted = record.get("s_is_deleted", Boolean::class.java) ?: false,
                    createdAt = record.get("s_created_at", java.time.Instant::class.java) ?: record[RESTROOMS.CREATED_AT]!!,
                    line = line
                )
            }

        return NearestRestroomResponseDto(
            id = record[RESTROOMS.ID]!!,
            name = record[RESTROOMS.NAME],
            address = record[RESTROOMS.ADDRESS]!!,
            coordinates = LatLon(lat = lat, lon = lon),
            distanceMeters = distanceMeters,
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            isOpen = isOpen,
            placeType = PlaceType.entries.find { p -> p.id == record.get(RESTROOMS.PLACE_TYPE) } ?: PlaceType.OTHER,
            building = building,
            subwayStation = station
        )
    }

    /**
     * Maps a database record to a slim DTO for nearest restrooms list.
     * Computes displayName from restroom.name or building.displayName.
     * Includes only minimal subway station info (displayName + lineColor).
     */
    fun mapToNearestRestroomSlim(
        record: Record,
        distanceMeters: Double
    ): NearestRestroomSlimDto {
        val lat = record.reqDouble("lat")
        val lon = record.reqDouble("lon")
        val restroomId = record[RESTROOMS.ID]!!
        val restroomName = record[RESTROOMS.NAME]?.trim()

        val displayName =
            when {
                !restroomName.isNullOrBlank() && restroomName != "Туалет" -> restroomName
                else -> {
                    val bName = record.get("b_name", String::class.java)?.trim()
                    val bAddress = record.get("b_address", String::class.java)?.trim()
                    when {
                        !bName.isNullOrBlank() -> bName
                        !bAddress.isNullOrBlank() -> bAddress
                        else -> "Туалет"
                    }
                }
            }

        val stationId = record.get("s_id") as? java.util.UUID
        val subwayStation =
            stationId?.let {
                val sNameRu = record.get("s_name_ru", String::class.java) ?: ""
                val sNameEn = record.get("s_name_en", String::class.java) ?: ""
                val sNameLocal = record.get("s_name_local", String::class.java)
                val sNameLocalLang = record.get("s_name_local_lang", String::class.java)
                val lineColorHex = record.get("l_hex", String::class.java)?.takeIf { it.isNotBlank() }

                val stationDisplayName =
                    when {
                        !sNameRu.isBlank() -> sNameRu
                        !sNameLocal.isNullOrBlank() -> sNameLocal
                        !sNameEn.isBlank() -> sNameEn
                        else -> null
                    }

                stationDisplayName?.let {
                    SubwayStationSlimDto(
                        displayName = it,
                        lineColorHex = lineColorHex
                    )
                }
            }

        return NearestRestroomSlimDto(
            id = restroomId,
            displayName = displayName,
            distanceMeters = distanceMeters,
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            coordinates = LatLon(lat = lat, lon = lon),
            subwayStation = subwayStation
        )
    }
}
