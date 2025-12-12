package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.cache.GeoKey;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
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
        GeoKey key = new GeoKey(lat, lon, radiusMeters, limit);
        var cached = cacheService.getNearestIds(key)
            .map(ids -> ids.stream()
                .map(cacheService::getRestroomInfo)
                .flatMap(Optional::stream)
                .toList())
            .orElse(List.of());

        if (!cached.isEmpty()) {
            return filterAndLimit(cached, radiusMeters, limit);
        }

        var result = backend.findNearest(lat, lon, limit, radiusMeters);

        var ids = result.stream().map(NearestRestroomResponseDto::id).toList();
        cacheService.putNearestIds(key, ids);
        result.forEach(dto -> cacheService.putRestroomInfo(dto.id(), dto));

        return filterAndLimit(result, radiusMeters, limit);
    }

    /**
     * Get restroom by ID
     * @param id restroom ID
     * @return restroom details
     */
    public Optional<by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto> getById(String id) {
        return backend.getById(id);
    }

    private List<NearestRestroomResponseDto> filterAndLimit(List<NearestRestroomResponseDto> source, int radiusMeters, int limit) {
        return source.stream()
            .filter(r -> r.distanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomResponseDto::distanceMeters))
            .limit(limit)
            .toList();
    }
}
