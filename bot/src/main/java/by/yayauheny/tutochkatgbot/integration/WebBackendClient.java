package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Web client implementation for backend integration
 */
@Component
public class WebBackendClient implements BackendClient {
    private final RestClient client;
    private final int retryAttempts;
    private final long retryDelayMs;

    public WebBackendClient(BackendProperties props) {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(props.connectTimeoutMs());
        factory.setReadTimeout(props.readTimeoutMs());

        this.client =
            RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .build();
        this.retryAttempts = Math.max(1, props.retryAttempts());
        this.retryDelayMs = Math.max(0L, props.retryDelayMs());
    }

    @Override
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit) {
        NearestRestroomResponseDto[] array =
            withRetry(() ->
                client.get()
                    .uri(uri -> uri.path("/restrooms/nearest")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("limit", limit)
                        .build())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NearestRestroomResponseDto[].class)
            );
        return array == null ? List.of() : Arrays.asList(array);
    }

    @Override
    public Optional<RestroomResponseDto> getById(String id) {
        RestroomResponseDto dto =
            withRetry(() ->
                client.get()
                    .uri("/restrooms/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(RestroomResponseDto.class)
            );
        return Optional.ofNullable(dto);
    }

    private <T> T withRetry(Supplier<T> action) {
        RestClientException last = null;
        for (int attempt = 1; attempt <= retryAttempts; attempt++) {
            try {
                return action.get();
            } catch (RestClientException ex) {
                last = ex;
                if (attempt == retryAttempts) break;
                sleepQuietly(retryDelayMs);
            }
        }
        if (last != null) {
            throw last;
        }
        return null;
    }

    private void sleepQuietly(long delayMs) {
        if (delayMs <= 0) return;
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
