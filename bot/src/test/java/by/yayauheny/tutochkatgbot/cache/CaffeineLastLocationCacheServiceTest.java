package by.yayauheny.tutochkatgbot.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CaffeineLastLocationCacheServiceTest {

    private CaffeineLastLocationCacheService service;

    @BeforeEach
    void setUp() {
        Cache<Long, LastLocation> cache = Caffeine.<Long, LastLocation>newBuilder()
            .expireAfterWrite(Duration.ofMinutes(20))
            .maximumSize(100)
            .build();
        service = new CaffeineLastLocationCacheService(cache);
    }

    @Test
    void shouldStoreAndRetrieveLastLocation() {
        long chatId = 123L;
        double lat = 53.9;
        double lon = 27.56;
        String source = "location";

        assertTrue(service.getLastLocation(chatId).isEmpty(), "Cache should be empty initially");

        service.putLastLocation(chatId, lat, lon, source);

        Optional<LastLocation> cached = service.getLastLocation(chatId);
        assertTrue(cached.isPresent(), "Location should be cached");
        assertEquals(lat, cached.get().latitude(), 0.0001);
        assertEquals(lon, cached.get().longitude(), 0.0001);
        assertEquals(source, cached.get().source());
        assertNotNull(cached.get().timestamp());
    }

    @Test
    void shouldEvictLocation() {
        long chatId = 123L;
        service.putLastLocation(chatId, 53.9, 27.56, "location");
        assertTrue(service.getLastLocation(chatId).isPresent(), "Location should be cached");

        service.evictLastLocation(chatId);
        assertTrue(service.getLastLocation(chatId).isEmpty(), "Cache should be empty after eviction");
    }

    @Test
    void shouldEvictAllLocations() {
        service.putLastLocation(1L, 53.9, 27.56, "location");
        service.putLastLocation(2L, 54.0, 27.60, "venue");
        
        assertTrue(service.getLastLocation(1L).isPresent());
        assertTrue(service.getLastLocation(2L).isPresent());

        service.evictAll();
        
        assertTrue(service.getLastLocation(1L).isEmpty(), "All locations should be evicted");
        assertTrue(service.getLastLocation(2L).isEmpty(), "All locations should be evicted");
    }

    @Test
    void shouldHandleDifferentSources() {
        long chatId = 123L;
        
        service.putLastLocation(chatId, 53.9, 27.56, "location");
        assertEquals("location", service.getLastLocation(chatId).get().source());

        service.putLastLocation(chatId, 54.0, 27.60, "venue");
        assertEquals("venue", service.getLastLocation(chatId).get().source());
        assertEquals(54.0, service.getLastLocation(chatId).get().latitude(), 0.0001);
    }
}

