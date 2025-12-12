package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Building response DTO
 */
public record BuildingResponseDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("cityId") UUID cityId,
    @JsonProperty("name") String name,
    @JsonProperty("address") String address,
    @JsonProperty("buildingType") PlaceType buildingType,
    @JsonProperty("workTime") Map<String, Object> workTime,
    @JsonProperty("coordinates") LatLon coordinates,
    @JsonProperty("externalIds") Map<String, Object> externalIds,
    @JsonProperty("isDeleted") Boolean isDeleted,
    @JsonProperty("createdAt") Instant createdAt,
    @JsonProperty("updatedAt") Instant updatedAt
) {
    public String displayName() {
        if (name != null && !name.isBlank()) {
            return name;
        }
        if (address != null && !address.isBlank()) {
            return address;
        }
        return null;
    }
}

