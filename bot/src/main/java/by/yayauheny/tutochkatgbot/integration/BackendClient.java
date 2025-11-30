package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * Interface for backend client.
 * Uses shared DTOs to avoid duplication between backend and bot modules.
 */
public interface BackendClient {
    List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit);
    Optional<RestroomResponseDto> getById(String id);
}
