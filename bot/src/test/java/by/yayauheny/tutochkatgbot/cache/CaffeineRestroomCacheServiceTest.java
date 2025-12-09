package by.yayauheny.tutochkatgbot.cache;

import by.yayauheny.shared.dto.LatLon;
import by.yayauheny.shared.dto.RestroomResponseDto;
import by.yayauheny.shared.enums.AccessibilityType;
import by.yayauheny.shared.enums.DataSourceType;
import by.yayauheny.shared.enums.FeeType;
import by.yayauheny.shared.enums.PlaceType;
import by.yayauheny.shared.enums.RestroomStatus;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaffeineRestroomCacheServiceTest {

    private final CaffeineRestroomCacheService service = new CaffeineRestroomCacheService(
        Caffeine.newBuilder().build(),
        Caffeine.<UUID, RestroomResponseDto>newBuilder().build()
    );

    @Test
    void nearestCacheShouldStoreAndRetrieveIds() {
        double lat = 53.9;
        double lon = 27.56;
        List<UUID> ids = List.of(UUID.randomUUID(), UUID.randomUUID());

        assertTrue(service.getNearestIds(lat, lon).isEmpty(), "Cache should be empty initially");

        service.putNearestIds(lat, lon, ids);

        Optional<List<UUID>> cached = service.getNearestIds(lat, lon);
        assertTrue(cached.isPresent());
        assertEquals(ids, cached.get());

        service.evictGeo();
        assertTrue(service.getNearestIds(lat, lon).isEmpty(), "Cache should be empty after eviction");
    }

    @Test
    void infoCacheShouldStoreAndRetrieveRestroom() {
        UUID id = UUID.randomUUID();
        RestroomResponseDto restroom = sampleRestroom(id);

        assertTrue(service.getRestroomInfo(id).isEmpty(), "Cache should be empty initially");

        service.putRestroomInfo(id, restroom);

        Optional<RestroomResponseDto> cached = service.getRestroomInfo(id);
        assertTrue(cached.isPresent());
        assertEquals(restroom.getId(), cached.get().getId());

        service.evictInfo();
        assertTrue(service.getRestroomInfo(id).isEmpty(), "Cache should be empty after eviction");
    }

    private RestroomResponseDto sampleRestroom(UUID id) {
        return new RestroomResponseDto(
            id,
            null,
            null,
            null,
            "Test",
            "Test address",
            null,
            null,
            FeeType.FREE,
            AccessibilityType.NONE,
            PlaceType.PUBLIC,
            new LatLon(53.9, 27.56),
            DataSourceType.MANUAL,
            RestroomStatus.ACTIVE,
            null,
            null,
            null,
            null,
            false,
            false,
            Instant.now(),
            Instant.now(),
            null,
            null,
            null
        );
    }
}
