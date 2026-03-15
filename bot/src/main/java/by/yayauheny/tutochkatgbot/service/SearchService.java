package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    public static final int DEFAULT_NEAREST_LIMIT = 5;

    private final BackendClient backend;

    public SearchService(BackendClient backend) {
        this.backend = backend;
    }

    public List<NearestRestroomSlimDto> findNearby(double lat, double lon, int radiusMeters, int limit) {
        logger.debug("Searching nearby: lat={}, lon={}, radius={}, limit={}", lat, lon, radiusMeters, limit);
        var result = backend.findNearest(lat, lon, limit, radiusMeters);
        logger.debug("Backend returned {} restrooms", result.size());
        return filterAndLimit(result, radiusMeters, limit);
    }

    public Optional<by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto> getById(String id) {
        try {
            return backend.getById(id);
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
