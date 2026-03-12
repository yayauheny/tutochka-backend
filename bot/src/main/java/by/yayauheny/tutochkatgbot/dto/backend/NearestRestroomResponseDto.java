package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Nearest restroom response DTO
 */
public record NearestRestroomResponseDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("name") String name,
    @JsonProperty("address") String address,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("distanceMeters") Double distanceMeters,
    @JsonProperty("feeType") FeeType feeType,
    @JsonProperty("isOpen") Boolean isOpen,
    @JsonProperty("placeType") PlaceType placeType,
    @JsonProperty("building") BuildingResponseDto building,
    @JsonProperty("subwayStation") SubwayStationResponseDto subwayStation
) {}

