package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for searching restrooms
 */
@Service
public class SearchService {
    private final BackendClient backend;
    private final RestroomCacheService cacheService;

    public SearchService(BackendClient backend, RestroomCacheService cacheService) {
        this.backend = backend;
        this.cacheService = cacheService;
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
        var cached = cacheService.getNearestIds(lat, lon)
            .map(ids -> ids.stream()
                .map(cacheService::getRestroomInfo)
                .flatMap(Optional::stream)
                .toList())
            .orElse(List.of());

        if (!cached.isEmpty()) {
            return filterAndLimit(cached, radiusMeters, limit);
        }

        var result = backend.findNearest(lat, lon, limit);

        var ids = result.stream().map(NearestRestroomResponseDto::getId).toList();
        cacheService.putNearestIds(lat, lon, ids);
        result.forEach(dto -> cacheService.putRestroomInfo(dto.getId(), dto));

        return filterAndLimit(result, radiusMeters, limit);
    }

    /**
     * Get restroom by ID
     * @param id restroom ID
     * @return restroom details
     */
    public Optional<by.yayauheny.shared.dto.RestroomResponseDto> getById(String id) {
        return backend.getById(id);
    }

    private List<NearestRestroomResponseDto> filterAndLimit(List<NearestRestroomResponseDto> source, int radiusMeters, int limit) {
        return source.stream()
            .filter(r -> r.getDistanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomResponseDto::getDistanceMeters))
            .limit(limit)
            .toList();
    }
}
