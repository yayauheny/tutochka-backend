package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Restroom response DTO.
 * feeType, genderType, accessibilityType, name, address may be null (backend stores them as optional).
 */
public record RestroomResponseDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("cityId") UUID cityId,
    @JsonProperty("buildingId") UUID buildingId,
    @JsonProperty("subwayStationId") UUID subwayStationId,
    @JsonProperty("name") String name,
    @JsonProperty("address") String address,
    @JsonProperty("phones") Map<String, Object> phones,
    @JsonProperty("workTime") Map<String, Object> workTime,
    @JsonProperty("feeType") FeeType feeType,
    @JsonProperty("genderType") GenderType genderType,
    @JsonProperty("accessibilityType") AccessibilityType accessibilityType,
    @JsonProperty("placeType") PlaceType placeType,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("dataSource") DataSourceType dataSource,
    @JsonProperty("status") RestroomStatus status,
    @JsonProperty("amenities") Map<String, Object> amenities,
    @JsonProperty("externalMaps") Map<String, Object> externalMaps,
    @JsonProperty("accessNote") String accessNote,
    @JsonProperty("directionGuide") String directionGuide,
    @JsonProperty("inheritBuildingSchedule") Boolean inheritBuildingSchedule,
    @JsonProperty("hasPhotos") Boolean hasPhotos,
    @JsonProperty("locationType") LocationType locationType,
    @JsonProperty("originProvider") ImportProvider originProvider,
    @JsonProperty("originId") String originId,
    @JsonProperty("isHidden") Boolean isHidden,
    @JsonProperty("createdAt") Instant createdAt,
    @JsonProperty("updatedAt") Instant updatedAt,
    @JsonProperty("distanceMeters") Integer distanceMeters,
    @JsonProperty("building") BuildingResponseDto building,
    @JsonProperty("subwayStation") SubwayStationResponseDto subwayStation
) {}

