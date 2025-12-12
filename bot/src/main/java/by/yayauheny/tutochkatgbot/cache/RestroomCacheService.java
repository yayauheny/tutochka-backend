package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestroomCacheService {
    Optional<List<UUID>> getNearestIds(GeoKey key);

    void putNearestIds(GeoKey key, List<UUID> ids);

    /**
     * Get restroom info from cache (slim DTO for list display)
     */
    Optional<NearestRestroomSlimDto> getRestroomInfo(UUID id);

    /**
     * Put restroom info to cache (slim DTO for list display)
     */
    void putRestroomInfo(UUID id, NearestRestroomSlimDto dto);

    Optional<RestroomResponseDto> getRestroomDetail(UUID id);

    void putRestroomDetail(UUID id, RestroomResponseDto dto);

    void evictGeo();

    void evictInfo();
    
    void evictDetail();
}
