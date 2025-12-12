package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomResponseDto;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.cache.CaffeineRestroomCacheService;
import by.yayauheny.tutochkatgbot.cache.GeoKey;
import by.yayauheny.tutochkatgbot.cache.RestroomCacheService;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceCacheKeyTest {

    private final RestroomCacheService cacheService = new CaffeineRestroomCacheService(
        Caffeine.newBuilder().build(),
        Caffeine.<UUID, NearestRestroomResponseDto>newBuilder().build()
    );

    private final BackendClient backend = Mockito.mock(BackendClient.class);

    private final SearchService service = new SearchService(backend, cacheService);

    @Test
    void cacheSeparatesByRadiusAndLimit() {
        NearestRestroomResponseDto dto = sampleNearest();
        when(backend.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(List.of(dto));

        // First call with radius 500 limit 5 -> backend invoked
        service.findNearby(53.9, 27.56, 500, 5);
        // Second call same params -> served from cache
        service.findNearby(53.9, 27.56, 500, 5);
        // Third call different radius -> backend invoked again
        service.findNearby(53.9, 27.56, 1000, 5);
        // Fourth call different limit -> backend invoked again
        service.findNearby(53.9, 27.56, 1000, 10);

        verify(backend, times(1)).findNearest(53.9, 27.56, 5, 500);
        verify(backend, times(1)).findNearest(53.9, 27.56, 5, 1000);
        verify(backend, times(1)).findNearest(53.9, 27.56, 10, 1000);
    }

    private NearestRestroomResponseDto sampleNearest() {
        return new NearestRestroomResponseDto(
            UUID.randomUUID(),
            "Test",
            "Addr",
            new LatLon(53.9, 27.56),
            10.0,
            FeeType.FREE,
            true,
            null,
            null,
            null
        );
    }
}
