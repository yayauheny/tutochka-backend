package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CaffeineRestroomCacheService implements RestroomCacheService {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineRestroomCacheService.class);

    private final Cache<GeoKey, List<UUID>> geoCache;
    private final Cache<UUID, NearestRestroomSlimDto> infoCache;
    private final Cache<UUID, RestroomResponseDto> detailCache;

    public CaffeineRestroomCacheService(
        Cache<GeoKey, List<UUID>> geoCache,
        Cache<UUID, NearestRestroomSlimDto> infoCache,
        Cache<UUID, RestroomResponseDto> detailCache
    ) {
        this.geoCache = geoCache;
        this.infoCache = infoCache;
        this.detailCache = detailCache;
    }

    @Override
    public Optional<List<UUID>> getNearestIds(GeoKey key) {
        var result = Optional.ofNullable(geoCache.getIfPresent(key));
        if (result.isPresent()) {
            logger.debug("CACHE HIT: geoCache key={}", key);
        } else {
            logger.debug("CACHE MISS: geoCache key={}", key);
        }
        return result;
    }

    @Override
    public void putNearestIds(GeoKey key, List<UUID> ids) {
        if (ids == null) {
            return;
        }
        geoCache.put(key, ids);
        logger.debug("CACHE PUT: geoCache key={}, ids count={}", key, ids.size());
    }

    @Override
    public Optional<NearestRestroomSlimDto> getRestroomInfo(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        var result = Optional.ofNullable(infoCache.getIfPresent(id));
        if (result.isPresent()) {
            logger.debug("CACHE HIT: infoCache id={}", id);
        } else {
            logger.debug("CACHE MISS: infoCache id={}", id);
        }
        return result;
    }

    @Override
    public void putRestroomInfo(UUID id, NearestRestroomSlimDto dto) {
        if (id == null || dto == null) {
            return;
        }
        infoCache.put(id, dto);
        logger.debug("CACHE PUT: infoCache id={}", id);
    }

    @Override
    public Optional<RestroomResponseDto> getRestroomDetail(UUID id) {
        if (id == null) {
            return Optional.empty();
        }
        var result = Optional.ofNullable(detailCache.getIfPresent(id));
        if (result.isPresent()) {
            logger.debug("CACHE HIT: detailCache id={}", id);
        } else {
            logger.debug("CACHE MISS: detailCache id={}", id);
        }
        return result;
    }

    @Override
    public void putRestroomDetail(UUID id, RestroomResponseDto dto) {
        if (id == null || dto == null) {
            return;
        }
        detailCache.put(id, dto);
        logger.debug("CACHE PUT: detailCache id={}", id);
    }

    @Override
    public void evictGeo() {
        geoCache.invalidateAll();
        logger.info("CACHE EVICT: geoCache invalidated");
    }

    @Override
    public void evictInfo() {
        infoCache.invalidateAll();
        logger.info("CACHE EVICT: infoCache invalidated");
    }

    @Override
    public void evictDetail() {
        detailCache.invalidateAll();
        logger.info("CACHE EVICT: detailCache invalidated");
    }

}
