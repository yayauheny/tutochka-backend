package by.yayauheny.tutochkatgbot.dto.backend;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

/**
 * Subway line response DTO
 */
public record SubwayLineResponseDto(
    @JsonProperty("id") UUID id,
    @JsonProperty("cityId") UUID cityId,
    @JsonProperty("nameRu") String nameRu,
    @JsonProperty("nameEn") String nameEn,
    @JsonProperty("shortCode") String shortCode,
    @JsonProperty("hexColor") String hexColor,
    @JsonProperty("isDeleted") Boolean isDeleted,
    @JsonProperty("createdAt") Instant createdAt
) {}

