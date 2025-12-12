package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestroomCacheService {
    Optional<List<UUID>> getNearestIds(GeoKey key);

    void putNearestIds(GeoKey key, List<UUID> ids);

    Optional<NearestRestroomResponseDto> getRestroomInfo(UUID id);

    void putRestroomInfo(UUID id, NearestRestroomResponseDto dto);

    void evictGeo();

    void evictInfo();
}
