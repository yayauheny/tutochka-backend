package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.metrics.BotMetrics;
import by.yayauheny.tutochkatgbot.handler.UpdateContext;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import yayauheny.by.contract.BackendClient;
import yayauheny.by.model.restroom.NearestRestroomSlimDto;
import yayauheny.by.model.restroom.RestroomResponseDto;
import io.micrometer.core.instrument.Timer;

@Service
public class SearchService {
    public static final int DEFAULT_NEAREST_LIMIT = 5;
    private static final Logger log = LoggerFactory.getLogger(SearchService.class);

    private final BackendClient backend;
    private final BotMetrics botMetrics;

    public SearchService(BackendClient backend, BotMetrics botMetrics) {
        this.backend = backend;
        this.botMetrics = botMetrics;
    }

    public List<NearestRestroomSlimDto> findNearby(double lat, double lon, int radiusMeters, int limit) {
        return findNearby(lat, lon, radiusMeters, limit, null);
    }

    public List<NearestRestroomSlimDto> findNearby(
        double lat,
        double lon,
        int radiusMeters,
        int limit,
        UpdateContext ctx
    ) {
        long startedAt = System.nanoTime();
        Timer.Sample sample = botMetrics.startBackendTimer();
        try {
            var result =
                ctx == null
                    ? backend.findNearest(lat, lon, limit, radiusMeters)
                    : backend.findNearest(lat, lon, limit, radiusMeters, ctx.userId(), ctx.chatId(), ctx.username());
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_NEAREST, "success");
            var filtered = filterAndLimit(result, radiusMeters, limit);
            long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
            log.info(
                "Backend nearest search completed: radiusMeters={}, limit={}, backendCount={}, returnedCount={}, durationMs={}",
                radiusMeters,
                limit,
                result.size(),
                filtered.size(),
                durationMs
            );
            return filtered;
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

    public Optional<RestroomResponseDto> getById(String id) {
        var sample = botMetrics.startBackendTimer();
        try {
            var result = backend.getById(id);
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, "success");
            return result;
        } catch (RestClientResponseException e) {
            botMetrics.incrementBackendRequest(BotMetrics.ENDPOINT_BY_ID, statusToOutcome(e.getRawStatusCode()));
            if (e.getStatusCode().value() >= 400 && e.getStatusCode().value() < 500) {
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
            .filter(r -> r.getDistanceMeters() <= radiusMeters)
            .sorted(Comparator.comparingDouble(NearestRestroomSlimDto::getDistanceMeters))
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
