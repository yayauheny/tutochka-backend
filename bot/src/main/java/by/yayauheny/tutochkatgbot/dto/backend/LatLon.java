package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Coordinates DTO
 */
public record LatLon(
    @JsonProperty("lat") Double lat,
    @JsonProperty("lon") Double lon
) {}

