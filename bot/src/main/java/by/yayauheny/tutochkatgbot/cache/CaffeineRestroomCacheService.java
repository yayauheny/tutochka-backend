package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CaffeineRestroomCacheService implements RestroomCacheService {

    private final Cache<GeoKey, List<UUID>> geoCache;
    private final Cache<UUID, NearestRestroomResponseDto> infoCache;

    public CaffeineRestroomCacheService(
        Cache<GeoKey, List<UUID>> geoCache,
        Cache<UUID, NearestRestroomResponseDto> infoCache
    ) {
        this.geoCache = geoCache;
        this.infoCache = infoCache;
    }

    @Override
    public Optional<List<UUID>> getNearestIds(GeoKey key) {
        return Optional.ofNullable(geoCache.getIfPresent(key));
    }

    @Override
    public void putNearestIds(GeoKey key, List<UUID> ids) {
        if (ids == null) {
            return;
        }
        geoCache.put(key, ids);
    }

    @Override
    public Optional<NearestRestroomResponseDto> getRestroomInfo(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(infoCache.getIfPresent(id));
    }

    @Override
    public void putRestroomInfo(UUID id, NearestRestroomResponseDto dto) {
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

}
