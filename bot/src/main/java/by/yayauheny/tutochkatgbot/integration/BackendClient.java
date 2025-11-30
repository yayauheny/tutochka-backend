package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.integration.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.integration.dto.RestroomResponseDto;

import java.util.List;
import java.util.Optional;

/**
 * Interface for backend client
 */
public interface BackendClient {
    List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit);
    Optional<RestroomResponseDto> getById(String id);
}
