package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.integration.BackendClient;
import by.yayauheny.tutochkatgbot.integration.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.integration.dto.RestroomResponseDto;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service for searching restrooms
 */
@Service
public class SearchService {
    private final BackendClient backend;

    public SearchService(BackendClient backend) {
        this.backend = backend;
    }

    /**
     * Find nearby restrooms within radius
     * @param lat latitude
     * @param lon longitude
     * @param radiusMeters radius in meters
     * @param limit maximum number of results
     * @return list of nearby restrooms
     */
    public List<NearestRestroomResponseDto> findNearby(double lat, double lon, int radiusMeters, int limit) {
        return backend.findNearest(lat, lon, limit).stream()
            .filter(r -> r.distanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingInt(NearestRestroomResponseDto::distanceMeters))
            .toList();
    }

    /**
     * Get restroom by ID
     * @param id restroom ID
     * @return restroom details
     */
    public Optional<RestroomResponseDto> getById(String id) {
        return backend.getById(id);
    }
}
