package yayauheny.by.common.mapper

import java.time.Instant
import org.jooq.Record
import org.jooq.UpdateSetFirstStep
import org.jooq.UpdateSetMoreStep
import yayauheny.by.model.dto.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.dto.NearestRestroomSlimDto
import yayauheny.by.model.dto.SubwayLineResponseDto
import yayauheny.by.model.dto.SubwayStationResponseDto
import yayauheny.by.model.dto.SubwayStationSlimDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.restroom.NearestRestroomResponseDto
import yayauheny.by.model.restroom.RestroomResponseDto
import yayauheny.by.model.restroom.RestroomUpdateDto
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.reqDouble
import yayauheny.by.util.setIfNotNull
import yayauheny.by.util.toEnumOrDefault
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
            genderType = GenderType.valueOf(record[RESTROOMS.GENDER_TYPE]!!),
            accessibilityType = AccessibilityType.valueOf(record[RESTROOMS.ACCESSIBILITY_TYPE]!!),
            placeType = record[RESTROOMS.PLACE_TYPE]?.let { PlaceType.fromCode(it) },
            coordinates = Coordinates(lat = lat, lon = lon),
            dataSource = record[RESTROOMS.DATA_SOURCE].toEnumOrDefault(DataSourceType.UNKNOWN),
            status = RestroomStatus.valueOf(record[RESTROOMS.STATUS]!!),
            amenities = record[RESTROOMS.AMENITIES].toJsonObjectOrEmpty(),
            externalMaps = record[RESTROOMS.EXTERNAL_MAPS].toJsonObjectOrEmpty(),
            accessNote = record[RESTROOMS.ACCESS_NOTE],
            directionGuide = record[RESTROOMS.DIRECTION_GUIDE],
            inheritBuildingSchedule = record[RESTROOMS.INHERIT_BUILDING_SCHEDULE] ?: false,
            hasPhotos = record[RESTROOMS.HAS_PHOTOS] ?: false,
            locationType = record[RESTROOMS.LOCATION_TYPE].toEnumOrDefault(LocationType.UNKNOWN),
            originProvider = record[RESTROOMS.ORIGIN_PROVIDER] ?: "MANUAL",
            originId = record[RESTROOMS.ORIGIN_ID],
            isHidden = record[RESTROOMS.IS_HIDDEN] ?: false,
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
                val bType = PlaceType.fromCode(bTypeRaw)

                BuildingResponseDto(
                    id = it,
                    cityId = record.get("b_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                    name = record.get("b_name", String::class.java),
                    address = bAddress,
                    buildingType = bType,
                    workTime = record.get("b_work_time", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    coordinates = if (bLat != null && bLon != null) Coordinates(bLat, bLon) else Coordinates(lat, lon),
                    externalIds = record.get("b_external_ids", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    isDeleted = record.get("b_is_deleted", Boolean::class.java) ?: false,
                    createdAt =
                        record.get("b_created_at", Instant::class.java)
                            ?: record[RESTROOMS.CREATED_AT]!!,
                    updatedAt =
                        record.get("b_updated_at", Instant::class.java)
                            ?: record[RESTROOMS.UPDATED_AT]!!
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
                            createdAt =
                                record.get("l_created_at", Instant::class.java)
                                    ?: record[RESTROOMS.CREATED_AT]!!
                        )
                    }
                SubwayStationResponseDto(
                    id = stationId,
                    subwayLineId = lineId ?: stationId, // fallback just to keep non-null id
                    nameRu = record.get("s_name_ru", String::class.java) ?: "",
                    nameEn = record.get("s_name_en", String::class.java) ?: "",
                    isTransfer = record.get("s_is_transfer", Boolean::class.java) ?: false,
                    coordinates = if (sLat != null && sLon != null) Coordinates(sLat, sLon) else Coordinates(lat, lon),
                    isDeleted = record.get("s_is_deleted", Boolean::class.java) ?: false,
                    createdAt =
                        record.get("s_created_at", Instant::class.java)
                            ?: record[RESTROOMS.CREATED_AT]!!,
                    line = line
                )
            }

        val genderTypeStr = record.get("gender_type", String::class.java) ?: "UNKNOWN"
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
            placeType = PlaceType.fromCode(record[RESTROOMS.PLACE_TYPE]),
            coordinates = Coordinates(lat = lat, lon = lon),
            dataSource = DataSourceType.valueOf(record[RESTROOMS.DATA_SOURCE]!!),
            status = RestroomStatus.valueOf(record[RESTROOMS.STATUS]!!),
            amenities = record[RESTROOMS.AMENITIES].toJsonObjectOrEmpty(),
            externalMaps = record[RESTROOMS.EXTERNAL_MAPS].toJsonObjectOrEmpty(),
            accessNote = record[RESTROOMS.ACCESS_NOTE],
            directionGuide = record[RESTROOMS.DIRECTION_GUIDE],
            inheritBuildingSchedule = record[RESTROOMS.INHERIT_BUILDING_SCHEDULE] ?: false,
            hasPhotos = record[RESTROOMS.HAS_PHOTOS] ?: false,
            locationType = record[RESTROOMS.LOCATION_TYPE].toEnumOrDefault(LocationType.UNKNOWN),
            originProvider = record[RESTROOMS.ORIGIN_PROVIDER] ?: "MANUAL",
            originId = record[RESTROOMS.ORIGIN_ID]?.toString(),
            isHidden = record[RESTROOMS.IS_HIDDEN] ?: false,
            createdAt = record[RESTROOMS.CREATED_AT]!!,
            updatedAt = record[RESTROOMS.UPDATED_AT]!!,
            building = building,
            subwayStation = station
        )
    }

    fun applyUpdateDto(
        updateStep: UpdateSetFirstStep<*>,
        dto: RestroomUpdateDto
    ): UpdateSetMoreStep<*> =
        updateStep
            .set(RESTROOMS.ADDRESS, dto.address)
            .set(RESTROOMS.INHERIT_BUILDING_SCHEDULE, dto.inheritBuildingSchedule)
            .set(RESTROOMS.FEE_TYPE, dto.feeType.name)
            .set(RESTROOMS.STATUS, dto.status.name)
            .set(RESTROOMS.UPDATED_AT, Instant.now())
            .set(RESTROOMS.HAS_PHOTOS, dto.hasPhotos)
            .setIfNotNull(RESTROOMS.PLACE_TYPE, dto.placeType?.code)
            .setIfNotNull(RESTROOMS.BUILDING_ID, dto.buildingId)
            .setIfNotNull(RESTROOMS.SUBWAY_STATION_ID, dto.subwayStationId)
            .setIfNotNull(RESTROOMS.GENDER_TYPE, dto.genderType?.name)
            .setIfNotNull(RESTROOMS.ACCESSIBILITY_TYPE, dto.accessibilityType?.name)
            .setIfNotNull(RESTROOMS.NAME, dto.name)
            .setIfNotNull(RESTROOMS.PHONES, dto.phones?.toJSONBOrEmpty())
            .setIfNotNull(RESTROOMS.WORK_TIME, dto.workTime?.toJSONBOrEmpty())
            .setIfNotNull(RESTROOMS.AMENITIES, dto.amenities?.toJSONBOrEmpty())
            .setIfNotNull(RESTROOMS.EXTERNAL_MAPS, dto.externalMaps?.toJSONBOrEmpty())
            .setIfNotNull(RESTROOMS.ACCESS_NOTE, dto.accessNote)
            .setIfNotNull(RESTROOMS.DIRECTION_GUIDE, dto.directionGuide)
            .setIfNotNull(RESTROOMS.LOCATION_TYPE, dto.locationType?.name)
            .setIfNotNull(RESTROOMS.ORIGIN_PROVIDER, dto.originProvider)
            .setIfNotNull(RESTROOMS.ORIGIN_ID, dto.originId)
            .setIfNotNull(RESTROOMS.IS_HIDDEN, dto.isHidden)

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
                val bType = PlaceType.fromCode(bTypeRaw)

                BuildingResponseDto(
                    id = it,
                    cityId = record.get("b_city_id", java.util.UUID::class.java) ?: record[RESTROOMS.CITY_ID]!!,
                    name = record.get("b_name", String::class.java),
                    address = bAddress,
                    buildingType = bType,
                    workTime = record.get("b_work_time", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    coordinates = if (bLat != null && bLon != null) Coordinates(bLat, bLon) else Coordinates(lat, lon),
                    externalIds = record.get("b_external_ids", org.jooq.JSONB::class.java)?.toJsonObjectOrEmpty(),
                    isDeleted = record.get("b_is_deleted", Boolean::class.java) ?: false,
                    createdAt =
                        record.get("b_created_at", Instant::class.java)
                            ?: record[RESTROOMS.CREATED_AT]!!,
                    updatedAt =
                        record.get("b_updated_at", Instant::class.java)
                            ?: record[RESTROOMS.UPDATED_AT]!!
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
                            createdAt =
                                record.get("l_created_at", Instant::class.java)
                                    ?: record[RESTROOMS.CREATED_AT]!!
                        )
                    }
                SubwayStationResponseDto(
                    id = stationId,
                    subwayLineId = lineId ?: stationId, // fallback just to keep non-null id
                    nameRu = record.get("s_name_ru", String::class.java) ?: "",
                    nameEn = record.get("s_name_en", String::class.java) ?: "",
                    isTransfer = record.get("s_is_transfer", Boolean::class.java) ?: false,
                    coordinates = if (sLat != null && sLon != null) Coordinates(sLat, sLon) else Coordinates(lat, lon),
                    isDeleted = record.get("s_is_deleted", Boolean::class.java) ?: false,
                    createdAt =
                        record.get("s_created_at", Instant::class.java)
                            ?: record[RESTROOMS.CREATED_AT]!!,
                    line = line
                )
            }

        return NearestRestroomResponseDto(
            id = record[RESTROOMS.ID]!!,
            name = record[RESTROOMS.NAME],
            address = record[RESTROOMS.ADDRESS]!!,
            coordinates = Coordinates(lat = lat, lon = lon),
            distanceMeters = distanceMeters,
            feeType = FeeType.valueOf(record[RESTROOMS.FEE_TYPE]!!),
            isOpen = isOpen,
            placeType = PlaceType.entries.find { p -> p.code == record.get(RESTROOMS.PLACE_TYPE) } ?: PlaceType.OTHER,
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
                val lineColorHex = record.get("l_hex", String::class.java)?.takeIf { it.isNotBlank() }

                val stationDisplayName =
                    when {
                        !sNameRu.isBlank() -> sNameRu
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
            coordinates = Coordinates(lat = lat, lon = lon),
            subwayStation = subwayStation
        )
    }
}
