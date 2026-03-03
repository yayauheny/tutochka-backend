package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Minimal subway station info for list display.
 */
public record SubwayStationSlimDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("displayName") String displayName,
    @JsonProperty("lineColorHex") String lineColorHex
) {}

