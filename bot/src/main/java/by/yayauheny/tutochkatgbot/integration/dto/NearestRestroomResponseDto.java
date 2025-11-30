package by.yayauheny.tutochkatgbot.integration.dto;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for nearest restroom response
 */
public record NearestRestroomResponseDto(
    String id,
    String cityId,
    String name,
    String description,
    String address,
    Map<String, Object> phones,
    Map<String, Object> workTime,
    FeeType feeType,
    AccessibilityType accessibilityType,
    double lat,
    double lon,
    DataSource dataSource,
    RestroomStatus status,
    Map<String, Object> amenities,
    Instant createdAt,
    Instant updatedAt,
    int distanceMeters
) {}
