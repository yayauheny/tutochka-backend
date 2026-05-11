package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import kotlinx.serialization.json.Json;
import kotlinx.serialization.json.JsonElementKt;
import kotlinx.serialization.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import yayauheny.by.contract.BackendClient;
import yayauheny.by.model.analytics.AnalyticsSource;
import yayauheny.by.model.analytics.ProductAnalyticsEvent;
import yayauheny.by.model.analytics.AnalyticsEventRequest;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;
import yayauheny.by.model.restroom.RestroomResponseDto;

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

        ObjectMapper objectMapper = createObjectMapper();
        this.client =
            RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(factory)
                .messageConverters(converters ->
                    converters.addFirst(new MappingJackson2HttpMessageConverter(objectMapper))
                )
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
        return findNearest(lat, lon, limit, distanceMeters, null, null, null);
    }

    @Override
    public List<NearestRestroomSlimDto> findNearest(
        double lat,
        double lon,
        int limit,
        int distanceMeters,
        Long tgUserId,
        Long tgChatId,
        String username
    ) {
        NearestRestroomSlimDto[] array =
            withRetry(() ->
                applyTelegramHeaders(
                    client.get()
                        .uri(uri -> uri.path("/restrooms/nearest")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("limit", limit)
                            .queryParam("distanceMeters", distanceMeters)
                            .build()),
                    tgUserId,
                    tgChatId,
                    username
                )
                    .header("X-Client-Type", "telegram_bot")
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(NearestRestroomSlimDto[].class)
            );
        return array == null ? List.of() : Arrays.asList(array);
    }

    @NotNull
    @Override
    public Optional<RestroomResponseDto> getById(@NotNull String id) {
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

    @Override
    public void trackProductEvent(
            @NotNull AnalyticsEventRequest request,
            Long tgUserId,
            Long tgChatId,
            String username
    ) {
        applyTelegramHeaders(
            client.post()
                .uri("/analytics/events")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request),
            tgUserId,
            tgChatId,
            username
        )
            .header("X-Client-Type", "telegram_bot")
            .retrieve()
            .toBodilessEntity();
    }

    private <T> T withRetry(Supplier<T> action) {
        try {
            return Retry.decorateSupplier(retry, action).get();
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
            return resp.getStatusCode().value() >= 500;
        }
        return ex instanceof ResourceAccessException;
    }

    private RestClient.RequestHeadersSpec<?> applyTelegramHeaders(
        RestClient.RequestHeadersSpec<?> request,
        Long tgUserId,
        Long tgChatId,
        String username
    ) {
        if (tgUserId != null) {
            request = request.header("X-Tg-User-Id", tgUserId.toString());
        }
        if (tgChatId != null) {
            request = request.header("X-Tg-Chat-Id", tgChatId.toString());
        }
        if (username != null && !username.isBlank()) {
            request = request.header("X-Tg-Username", username);
        }
        return request.header("X-Analytics-Source", AnalyticsSource.TELEGRAM_BOT.getValue());
    }

    private static ObjectMapper createObjectMapper() {
        SimpleModule module = new SimpleModule()
            .addSerializer(ProductAnalyticsEvent.class, new ProductAnalyticsEventSerializer())
            .addSerializer(JsonObject.class, new JsonObjectSerializer())
            .addDeserializer(JsonObject.class, new JsonObjectDeserializer());

        return com.fasterxml.jackson.databind.json.JsonMapper.builder()
            .addModule(new KotlinModule.Builder().build())
            .addModule(new JavaTimeModule())
            .addModule(module)
            .build();
    }

    private static final class JsonObjectSerializer extends JsonSerializer<JsonObject> {
        @Override
        public void serialize(JsonObject value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeRawValue(value.toString());
        }
    }

    private static final class JsonObjectDeserializer extends JsonDeserializer<JsonObject> {
        @Override
        public JsonObject deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.getCodec().readTree(p);
            if (node == null || node.isNull()) {
                return null;
            }
            return JsonElementKt.getJsonObject(Json.Default.parseToJsonElement(node.toString()));
        }
    }

    private static final class ProductAnalyticsEventSerializer extends JsonSerializer<ProductAnalyticsEvent> {
        @Override
        public void serialize(ProductAnalyticsEvent value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.getValue());
        }
    }
}
