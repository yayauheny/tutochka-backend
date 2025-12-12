package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Minimal subway station info for list display.
 */
public record SubwayStationSlimDto(
    @JsonProperty("displayName") String displayName,
    @JsonProperty("lineColorHex") String lineColorHex
) {}

