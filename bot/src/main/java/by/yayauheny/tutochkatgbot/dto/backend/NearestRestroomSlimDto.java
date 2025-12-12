package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Slim DTO for nearest restrooms list.
 * Contains only fields necessary for list UI and caching.
 */
public record NearestRestroomSlimDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("displayName") String displayName,
    @JsonProperty("distanceMeters") Double distanceMeters,
    @JsonProperty("feeType") FeeType feeType,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("subwayStation") SubwayStationSlimDto subwayStation
) {}

