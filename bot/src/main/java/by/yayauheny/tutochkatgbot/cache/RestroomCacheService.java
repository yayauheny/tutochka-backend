package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.shared.dto.RestroomResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RestroomCacheService {
    Optional<List<UUID>> getNearestIds(double latitude, double longitude);

    void putNearestIds(double latitude, double longitude, List<UUID> ids);

    Optional<RestroomResponseDto> getRestroomInfo(UUID id);

    void putRestroomInfo(UUID id, RestroomResponseDto dto);

    void evictGeo();

    void evictInfo();
}
