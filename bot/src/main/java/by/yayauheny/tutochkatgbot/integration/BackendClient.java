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
    /**
     * Find nearest restrooms
     * @param lat latitude
     * @param lon longitude
     * @param limit maximum number of results
     * @param distanceMeters maximum distance in meters
     * @return list of nearest restrooms
     */
    List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit, int distanceMeters);
    
    /**
     * Get restroom by ID
     * @param id restroom ID
     * @return restroom details
     */
    Optional<RestroomResponseDto> getById(String id);
}
