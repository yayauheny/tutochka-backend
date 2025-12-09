package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.shared.dto.LatLon;
import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
                .map(dto -> toNearest(dto, lat, lon))
                .filter(Objects::nonNull)
                .collect(Collectors.toList()))
            .orElse(List.of());

        if (!cached.isEmpty()) {
            return filterAndLimit(cached, radiusMeters, limit);
        }

        var result = backend.findNearest(lat, lon, limit);

        cacheService.putNearestIds(
            lat,
            lon,
            result.stream()
                .map(NearestRestroomResponseDto::getId)
                .toList()
        );

        return filterAndLimit(result, radiusMeters, limit);
    }

    /**
     * Get restroom by ID
     * @param id restroom ID
     * @return restroom details
     */
    public Optional<RestroomResponseDto> getById(String id) {
        if (id == null) {
            return Optional.empty();
        }

        UUID uuid;
        try {
            uuid = UUID.fromString(id);
        } catch (IllegalArgumentException ex) {
            return backend.getById(id);
        }

        var cached = cacheService.getRestroomInfo(uuid);
        if (cached.isPresent()) {
            return cached;
        }

        var fetched = backend.getById(id);
        fetched.ifPresent(dto -> cacheService.putRestroomInfo(uuid, dto));
        return fetched;
    }

    private List<NearestRestroomResponseDto> filterAndLimit(List<NearestRestroomResponseDto> source, int radiusMeters, int limit) {
        return source.stream()
            .filter(r -> r.getDistanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomResponseDto::getDistanceMeters))
            .limit(limit)
            .toList();
    }

    private NearestRestroomResponseDto toNearest(RestroomResponseDto restroom, double queryLat, double queryLon) {
        if (restroom == null || restroom.getCoordinates() == null) {
            return null;
        }

        LatLon coords = restroom.getCoordinates();
        double distanceMeters = haversineMeters(queryLat, queryLon, coords.getLat(), coords.getLon());

        return new NearestRestroomResponseDto(
            restroom.getId(),
            restroom.getName(),
            restroom.getAddress(),
            coords,
            distanceMeters,
            restroom.getFeeType(),
            null,
            restroom.getPlaceType(),
            restroom.getBuilding(),
            restroom.getSubwayStation()
        );
    }

    private double haversineMeters(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadius = 6_371_000d;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }
}
