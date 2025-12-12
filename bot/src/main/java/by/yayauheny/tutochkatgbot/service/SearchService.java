package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.cache.GeoKey;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    
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
     * @return list of nearby restrooms (slim DTO)
     */
    public List<NearestRestroomSlimDto> findNearby(double lat, double lon, int radiusMeters, int limit) {
        GeoKey key = new GeoKey(lat, lon, radiusMeters, limit);
        logger.debug("Searching nearby: lat={}, lon={}, radius={}, limit={}", lat, lon, radiusMeters, limit);
        
        var cached = cacheService.getNearestIds(key)
            .map(ids -> ids.stream()
                .map(cacheService::getRestroomInfo)
                .flatMap(Optional::stream)
                .toList())
            .orElse(List.of());

        if (!cached.isEmpty()) {
            logger.debug("Using cached results: {} restrooms", cached.size());
            return filterAndLimit(cached, radiusMeters, limit);
        }

        logger.debug("Cache miss, fetching from backend");
        var result = backend.findNearest(lat, lon, limit, radiusMeters);
        logger.debug("Backend returned {} restrooms", result.size());

        var ids = result.stream().map(NearestRestroomSlimDto::id).toList();
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
        try {
            UUID uuid = UUID.fromString(id);
            var cached = cacheService.getRestroomDetail(uuid);
            if (cached.isPresent()) {
                return cached;
            }

            var result = backend.getById(id);
            result.ifPresent(dto -> cacheService.putRestroomDetail(uuid, dto));
            return result;
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private List<NearestRestroomSlimDto> filterAndLimit(List<NearestRestroomSlimDto> source, int radiusMeters, int limit) {
        return source.stream()
            .filter(r -> r.distanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomSlimDto::distanceMeters))
            .limit(limit)
            .toList();
    }
}
