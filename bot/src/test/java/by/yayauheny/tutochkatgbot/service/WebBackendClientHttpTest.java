package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.config.BackendProperties;
import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto;
import by.yayauheny.tutochkatgbot.integration.WebBackendClient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebBackendClientHttpTest {
    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void findNearestShouldCallBackendAndMapResponse() {
        UUID restroomId = UUID.randomUUID();
        AtomicReference<String> requestMethod = new AtomicReference<>();
        AtomicReference<String> clientTypeHeader = new AtomicReference<>();
        AtomicReference<String> rawQuery = new AtomicReference<>();

        server.createContext("/api/v1/restrooms/nearest", exchange -> {
            requestMethod.set(exchange.getRequestMethod());
            clientTypeHeader.set(exchange.getRequestHeaders().getFirst("X-Client-Type"));
            rawQuery.set(exchange.getRequestURI().getRawQuery());
            respond(exchange, 200, nearestResponseJson(restroomId));
        });

        WebBackendClient client = new WebBackendClient(properties(1, 0, 1_000));

        List<NearestRestroomSlimDto> result = client.findNearest(53.9, 27.56, 5, 500);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo(restroomId);
        assertThat(result.get(0).displayName()).isEqualTo("Test restroom");
        assertThat(requestMethod.get()).isEqualTo("GET");
        assertThat(clientTypeHeader.get()).isEqualTo("telegram_bot");
        assertThat(parseQuery(rawQuery.get()))
            .containsEntry("lat", "53.9")
            .containsEntry("lon", "27.56")
            .containsEntry("limit", "5")
            .containsEntry("distanceMeters", "500");
    }

    @Test
    void getByIdShouldRetryOnServerErrorUntilSuccess() {
        UUID restroomId = UUID.randomUUID();
        AtomicInteger attempts = new AtomicInteger();

        server.createContext("/api/v1/restrooms/" + restroomId, exchange -> {
            int attempt = attempts.incrementAndGet();
            if (attempt < 3) {
                respond(exchange, 500, "");
                return;
            }
            respond(exchange, 200, restroomResponseJson(restroomId));
        });

        WebBackendClient client = new WebBackendClient(properties(3, 0, 1_000));

        Optional<RestroomResponseDto> result = client.getById(restroomId.toString());

        assertThat(result).isPresent();
        assertThat(result.orElseThrow().id()).isEqualTo(restroomId);
        assertThat(attempts.get()).isEqualTo(3);
    }

    private BackendProperties properties(int retryAttempts, long retryDelayMs, int timeoutMs) {
        return new BackendProperties(baseUrl(), timeoutMs, timeoutMs, retryAttempts, retryDelayMs);
    }

    private String baseUrl() {
        return "http://localhost:%d/api/v1".formatted(server.getAddress().getPort());
    }

    private Map<String, String> parseQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return Map.of();
        }
        return Arrays.stream(rawQuery.split("&"))
            .map(pair -> pair.split("=", 2))
            .collect(
                Collectors.toMap(
                    pair -> decode(pair[0]),
                    pair -> pair.length > 1 ? decode(pair[1]) : "",
                    (first, second) -> second,
                    LinkedHashMap::new
                )
            );
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        } finally {
            exchange.close();
        }
    }

    private String nearestResponseJson(UUID restroomId) {
        return """
            [
              {
                "id": "%s",
                "displayName": "Test restroom",
                "distanceMeters": 123.4,
                "feeType": "FREE",
                "queryCoordinates": {"lat": 53.9, "lon": 27.56},
                "restroomCoordinates": {"lat": 53.91, "lon": 27.57}
              }
            ]
            """.formatted(restroomId);
    }

    private String restroomResponseJson(UUID restroomId) {
        return """
            {
              "id": "%s",
              "cityId": "%s",
              "cityName": "Minsk",
              "buildingId": null,
              "subwayStationId": null,
              "name": "Retry restroom",
              "address": "Address",
              "phones": {},
              "workTime": {},
              "feeType": "FREE",
              "genderType": null,
              "accessibilityType": "UNKNOWN",
              "placeType": "OTHER",
              "coordinates": {"lat": 53.9, "lon": 27.56},
              "dataSource": "USER",
              "status": "ACTIVE",
              "amenities": {},
              "externalMaps": {},
              "accessNote": null,
              "directionGuide": null,
              "inheritBuildingSchedule": false,
              "hasPhotos": false,
              "locationType": "UNKNOWN",
              "originProvider": "USER",
              "originId": null,
              "isHidden": false,
              "createdAt": "2025-01-01T00:00:00Z",
              "updatedAt": "2025-01-01T00:00:00Z",
              "distanceMeters": 100,
              "building": null,
              "subwayStation": null
            }
            """.formatted(restroomId, UUID.randomUUID());
    }
}
