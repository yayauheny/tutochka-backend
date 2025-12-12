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
    @JsonProperty("nameLocal") String nameLocal,
    @JsonProperty("nameLocalLang") String nameLocalLang,
    @JsonProperty("isTransfer") Boolean isTransfer,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("isDeleted") Boolean isDeleted,
    @JsonProperty("createdAt") Instant createdAt,
    @JsonProperty("line") SubwayLineResponseDto line
) {
    public String displayName(String preferredLang) {
        String lang = preferredLang != null ? preferredLang.toLowerCase().trim() : null;
        String normalizedLocalLang = nameLocalLang != null ? nameLocalLang.toLowerCase().trim() : null;

        String[] candidates;
        if (lang != null && lang.equals(normalizedLocalLang)) {
            candidates = new String[]{nameLocal, nameRu, nameEn};
        } else if ("ru".equalsIgnoreCase(lang)) {
            candidates = new String[]{nameRu, nameLocal, nameEn};
        } else if ("en".equalsIgnoreCase(lang)) {
            candidates = new String[]{nameEn, nameLocal, nameRu};
        } else {
            candidates = new String[]{nameLocal, nameEn, nameRu};
        }

        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate.trim();
            }
        }
        return "Unknown";
    }

    public String lineColor() {
        return line != null && line.hexColor() != null && !line.hexColor().isBlank()
            ? line.hexColor()
            : null;
    }
}

