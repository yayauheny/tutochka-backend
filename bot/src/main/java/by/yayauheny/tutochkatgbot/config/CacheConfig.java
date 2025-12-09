package by.yayauheny.tutochkatgbot.config;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.cache.GeoKey;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Cache configuration for geo and restroom info caches.
 */
@Configuration
public class CacheConfig {

    @Bean
    public Cache<GeoKey, List<UUID>> geoRestroomCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(60))
            .maximumSize(300)
            .recordStats()
            .build();
    }

    @Bean
    public Cache<UUID, NearestRestroomResponseDto> restroomInfoCache() {
        return Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(12))
            .maximumSize(100)
            .recordStats()
            .build();
    }
}
