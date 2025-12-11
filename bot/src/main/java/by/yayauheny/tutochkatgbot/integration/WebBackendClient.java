package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpClient;
import java.time.Duration;
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
    private final Retry retry;

    public WebBackendClient(BackendProperties props) {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(props.connectTimeoutMs()))
            .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(props.readTimeoutMs()));

        this.client =
            RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .build();

        RetryConfig retryConfig = RetryConfig.<Object>custom()
            .maxAttempts(Math.max(1, props.retryAttempts()))
            .waitDuration(Duration.ofMillis(Math.max(0L, props.retryDelayMs())))
            .retryOnException(this::shouldRetry)
            .build();
        this.retry = RetryRegistry.of(retryConfig).retry("backendClient");
    }

    @Override
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit, int distanceMeters) {
        NearestRestroomResponseDto[] array =
            withRetry(() ->
                client.get()
                    .uri(uri -> uri.path("/restrooms/nearest")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("limit", limit)
                        .queryParam("distanceMeters", distanceMeters)
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
        Supplier<T> decorated = Retry.decorateSupplier(retry, action);
        try {
            return decorated.get();
        } catch (RuntimeException ex) {
            // Unwrap Resilience4j exceptions
            Throwable cause = ex.getCause();
            if (cause instanceof RestClientException) {
                throw (RestClientException) cause;
            }
            throw ex;
        }
    }

    private boolean shouldRetry(Throwable throwable) {
        if (!(throwable instanceof RestClientException ex)) {
            return false;
        }
        // Don't retry 4xx errors (client errors)
        if (ex instanceof RestClientResponseException resp) {
            int status = resp.getRawStatusCode();
            // Only retry 5xx errors (server errors)
            return status >= 500;
        }
        // Retry timeouts / connection issues
        return ex instanceof ResourceAccessException;
    }
}
