package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
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
    public List<NearestRestroomSlimDto> findNearest(double lat, double lon, int limit, int distanceMeters) {
        NearestRestroomSlimDto[] array =
            withRetry(() ->
                client.get()
                    .uri(uri -> uri.path("/restrooms/nearest")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("limit", limit)
                        .queryParam("distanceMeters", distanceMeters)
                        .build())
                    .header("X-Client-Type", "telegram_bot")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NearestRestroomSlimDto[].class)
            );
        return array == null ? List.of() : Arrays.asList(array);
    }

    @Override
    public Optional<RestroomResponseDto> getById(String id) {
        RestroomResponseDto dto =
            withRetry(() ->
                client.get()
                    .uri("/restrooms/{id}", id)
                    .header("X-Client-Type", "telegram_bot")
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
        if (ex instanceof RestClientResponseException resp) {
            int status = resp.getRawStatusCode();
            return status >= 500;
        }
        return ex instanceof ResourceAccessException;
    }
}
