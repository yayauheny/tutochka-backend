package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Subway station response DTO
 */
public record SubwayStationResponseDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("subwayLineId") UUID subwayLineId,
    @JsonProperty("nameRu") String nameRu,
    @JsonProperty("nameEn") String nameEn,
    @JsonProperty("isTransfer") Boolean isTransfer,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("isDeleted") Boolean isDeleted,
    @JsonProperty("createdAt") Instant createdAt,
    @JsonProperty("line") SubwayLineResponseDto line
) {
    public String displayName(String preferredLang) {
        if ("en".equalsIgnoreCase(preferredLang) && nameEn != null && !nameEn.isBlank()) {
            return nameEn.trim();
        }
        return nameRu != null && !nameRu.isBlank() ? nameRu.trim() : (nameEn != null ? nameEn.trim() : "Unknown");
    }

    public String lineColor() {
        return line != null && line.hexColor() != null && !line.hexColor().isBlank()
            ? line.hexColor()
            : null;
    }
}

