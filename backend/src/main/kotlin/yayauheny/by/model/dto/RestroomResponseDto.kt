package yayauheny.by.model.dto

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import java.time.Instant
import java.util.UUID
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus

@Serializable
data class RestroomResponseDto(
    @Contextual val id: UUID,
    @Contextual val cityId: UUID?,
    @Contextual val buildingId: UUID?,
    @Contextual val subwayStationId: UUID?,
    val name: String?,
    val address: String,
    val phones: JsonObject?,
    val workTime: JsonObject?,
    val feeType: FeeType,
    val genderType: GenderType,
    val accessibilityType: AccessibilityType,
    val placeType: PlaceType?,
    val coordinates: LatLon,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: JsonObject?,
    val externalMaps: JsonObject?,
    val accessNote: String?,
    val directionGuide: String?,
    val inheritBuildingSchedule: Boolean,
    val hasPhotos: Boolean,
    @Contextual val createdAt: Instant,
    @Contextual val updatedAt: Instant,
    val distanceMeters: Int? = null,
    val building: BuildingResponseDto? = null,
    val subwayStation: SubwayStationResponseDto? = null
)

