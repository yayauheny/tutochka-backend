package by.yayauheny.tutochkatgbot.config;

import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.cache.GeoKey;
import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.binder.MeterBinder;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.UUID;

@Configuration
public class MetricsConfig {

    @Bean
    public MeterBinder cacheMeterBinder(
        Cache<GeoKey, List<UUID>> geoRestroomCache,
        Cache<UUID, RestroomResponseDto> restroomInfoCache
    ) {
        return registry -> {
            CaffeineCacheMetrics.monitor(registry, geoRestroomCache, "geo-nearest-restrooms");
            CaffeineCacheMetrics.monitor(registry, restroomInfoCache, "nearest-restrooms-info");
        };
    }
}
