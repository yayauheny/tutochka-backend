package by.yayauheny.tutochkatgbot.cache;

import com.github.benmanes.caffeine.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

/**
 * Caffeine-based implementation of LastLocationCacheService
 */
@Service
public class CaffeineLastLocationCacheService implements LastLocationCacheService {
    private static final Logger logger = LoggerFactory.getLogger(CaffeineLastLocationCacheService.class);

    private final Cache<Long, LastLocation> cache;

    public CaffeineLastLocationCacheService(Cache<Long, LastLocation> lastLocationCache) {
        this.cache = lastLocationCache;
    }

    @Override
    public Optional<LastLocation> getLastLocation(long chatId) {
        var result = Optional.ofNullable(cache.getIfPresent(chatId));
        if (result.isPresent()) {
            logger.debug("CACHE HIT: lastLocation cacheId={}", chatId);
        } else {
            logger.debug("CACHE MISS: lastLocation cacheId={}", chatId);
        }
        return result;
    }

    @Override
    public void putLastLocation(long chatId, double latitude, double longitude, String source) {
        LastLocation location = new LastLocation(latitude, longitude, Instant.now(), source);
        cache.put(chatId, location);
        logger.debug("CACHE PUT: lastLocation cacheId={}, lat={}, lon={}, source={}", chatId, latitude, longitude, source);
    }

    @Override
    public void evictLastLocation(long chatId) {
        cache.invalidate(chatId);
        logger.debug("CACHE EVICT: lastLocation cacheId={}", chatId);
    }

    @Override
    public void evictAll() {
        cache.invalidateAll();
        logger.debug("CACHE EVICT ALL: lastLocation");
    }
}


