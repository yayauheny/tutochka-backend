package yayauheny.by.model.restroom



import java.time.Instant
import java.util.UUID



import yayauheny.by.model.building.BuildingResponseDto
import yayauheny.by.model.dto.Coordinates
import yayauheny.by.model.subway.SubwayStationResponseDto
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.DataSourceType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus


data class RestroomResponseDto(
     val id: UUID,
     val cityId: UUID?,
    val cityName: String? = null,
     val buildingId: UUID?,
     val subwayStationId: UUID?,
    val name: String?,
    val address: String?,
    val phones: Map<String, Any>?,
    val workTime: Map<String, Any>?,
    val feeType: FeeType?,
    val genderType: GenderType?,
    val accessibilityType: AccessibilityType?,
    val placeType: PlaceType?,
    val coordinates: Coordinates,
    val dataSource: DataSourceType,
    val status: RestroomStatus,
    val amenities: Map<String, Any>?,
    val externalMaps: Map<String, Any>?,
    val accessNote: String?,
    val directionGuide: String?,
    val inheritBuildingSchedule: Boolean,
    val hasPhotos: Boolean,
    val locationType: LocationType,
    val originProvider: ImportProvider,
    val originId: String?,
    val isHidden: Boolean,
    val moderationStatus: String,
     val createdAt: Instant,
     val updatedAt: Instant,
    val distanceMeters: Int? = null,
    val building: BuildingResponseDto? = null,
    val subwayStation: SubwayStationResponseDto? = null
)
