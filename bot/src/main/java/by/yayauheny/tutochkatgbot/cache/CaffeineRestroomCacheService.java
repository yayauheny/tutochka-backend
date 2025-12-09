package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.shared.dto.RestroomResponseDto;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CaffeineRestroomCacheService implements RestroomCacheService {

    private final Cache<GeoKey, List<UUID>> geoCache;
    private final Cache<UUID, RestroomResponseDto> infoCache;

    public CaffeineRestroomCacheService(
        Cache<GeoKey, List<UUID>> geoCache,
        Cache<UUID, RestroomResponseDto> infoCache
    ) {
        this.geoCache = geoCache;
        this.infoCache = infoCache;
    }

    @Override
    public Optional<List<UUID>> getNearestIds(double latitude, double longitude) {
        return Optional.ofNullable(geoCache.getIfPresent(geoKey(latitude, longitude)));
    }

    @Override
    public void putNearestIds(double latitude, double longitude, List<UUID> ids) {
        if (ids == null) {
            return;
        }
        geoCache.put(geoKey(latitude, longitude), ids);
    }

    @Override
    public Optional<RestroomResponseDto> getRestroomInfo(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(infoCache.getIfPresent(id));
    }

    @Override
    public void putRestroomInfo(UUID id, RestroomResponseDto dto) {
        if (id == null || dto == null) {
            return;
        }
        infoCache.put(id, dto);
    }

    @Override
    public void evictGeo() {
        geoCache.invalidateAll();
    }

    @Override
    public void evictInfo() {
        infoCache.invalidateAll();
    }

    private GeoKey geoKey(double latitude, double longitude) {
        return new GeoKey(latitude, longitude);
    }
}
