package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.dto.backend.NearestRestroomSlimDto;
import by.yayauheny.tutochkatgbot.integration.BackendClient;
import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class SearchService {
    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);
    public static final int DEFAULT_NEAREST_LIMIT = 5;

    private final BackendClient backend;
    private final BotMetrics botMetrics;

    public SearchService(BackendClient backend, BotMetrics botMetrics) {
        this.backend = backend;
        this.botMetrics = botMetrics;
    }

    public List<NearestRestroomSlimDto> findNearby(double lat, double lon, int radiusMeters, int limit) {
        logger.debug("Searching nearby: lat={}, lon={}, radius={}, limit={}", lat, lon, radiusMeters, limit);
        Timer.Sample sample = botMetrics.startBackendTimer();
        try {
            var result = backend.findNearest(lat, lon, limit, radiusMeters);
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_NEAREST, "success");
            logger.debug("Backend returned {} restrooms", result.size());
            return filterAndLimit(result, radiusMeters, limit);
        } catch (RestClientResponseException e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_NEAREST, statusToOutcome(e.getRawStatusCode()));
            throw e;
        } catch (Exception e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_NEAREST, "error");
            throw e;
        } finally {
            botMetrics.stopBackendTimer(sample, BotMetrics.ENDPOINT_NEAREST);
        }
    }

    public Optional<by.yayauheny.tutochkatgbot.dto.backend.RestroomResponseDto> getById(String id) {
        Timer.Sample sample = botMetrics.startBackendTimer();
        try {
            var result = backend.getById(id);
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, "success");
            return result;
        } catch (RestClientResponseException e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, statusToOutcome(e.getRawStatusCode()));
            if (e.getRawStatusCode() >= 400 && e.getRawStatusCode() < 500) {
                return Optional.empty();
            }
            throw e;
        } catch (IllegalArgumentException e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, "4xx");
            return Optional.empty();
        } catch (Exception e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, "error");
            throw e;
        } finally {
            botMetrics.stopBackendTimer(sample, BotMetrics.ENDPOINT_BY_ID);
        }
    }

    private List<NearestRestroomSlimDto> filterAndLimit(List<NearestRestroomSlimDto> source, int radiusMeters, int limit) {
        return source.stream()
            .filter(r -> r.distanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomSlimDto::distanceMeters))
            .limit(limit)
            .toList();
    }

    private String statusToOutcome(int statusCode) {
        if (statusCode >= 400 && statusCode < 500) {
            return "4xx";
        }
        if (statusCode >= 500 && statusCode < 600) {
            return "5xx";
        }
        return "error";
    }
}
