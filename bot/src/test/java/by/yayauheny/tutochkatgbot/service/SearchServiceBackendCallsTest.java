package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.LatLon;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.FeeType;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SearchServiceBackendCallsTest {

    private final BackendClient backend = Mockito.mock(BackendClient.class);
    private final BotMetrics botMetrics = Mockito.mock(BotMetrics.class);
    private final SearchService service = new SearchService(backend, botMetrics);

    @Test
    void findNearbyCallsBackendEachTime() {
        NearestRestroomSlimDto dto = sampleNearest();
        when(backend.findNearest(anyDouble(), anyDouble(), anyInt(), anyInt()))
            .thenReturn(List.of(dto));

        service.findNearby(53.9, 27.56, 500, SearchService.DEFAULT_NEAREST_LIMIT);
        service.findNearby(53.9, 27.56, 500, SearchService.DEFAULT_NEAREST_LIMIT);
        service.findNearby(53.9, 27.56, 1000, SearchService.DEFAULT_NEAREST_LIMIT);

        verify(backend, times(3)).findNearest(anyDouble(), anyDouble(), anyInt(), anyInt());
        verify(backend, times(2)).findNearest(53.9, 27.56, 5, 500);
        verify(backend, times(1)).findNearest(53.9, 27.56, 5, 1000);
    }

    @Test
    void getByIdShouldReturnEmptyFor4xxErrors() {
        when(backend.getById("missing"))
            .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        assertTrue(service.getById("missing").isEmpty());
    }

    private NearestRestroomSlimDto sampleNearest() {
        return new NearestRestroomSlimDto(
            UUID.randomUUID(),
            "Test",
            10.0,
            FeeType.FREE,
            new LatLon(53.9, 27.56),
            new LatLon(53.9, 27.56)
        );
    }
}
