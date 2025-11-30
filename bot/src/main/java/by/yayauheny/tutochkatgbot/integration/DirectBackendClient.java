package by.yayauheny.tutochkatgbot.integration;

import by.yayauheny.shared.dto.NearestRestroomResponseDto;
import by.yayauheny.shared.dto.RestroomResponseDto;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.Dispatchers;
import kotlinx.coroutines.future.FutureKt;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import yayauheny.by.service.RestroomService;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Direct backend client implementation that calls backend services directly
 * without REST API calls. This eliminates network latency and improves performance.
 * 
 * Uses Kotlin coroutines to call suspend functions from Java.
 * 
 * This is the primary implementation - WebBackendClient is kept for fallback/testing.
 */
@Component
@Primary
public class DirectBackendClient implements BackendClient {
    private final RestroomService restroomService;
    private final CoroutineScope coroutineScope;

    public DirectBackendClient(RestroomService restroomService) {
        this.restroomService = restroomService;
        CoroutineContext context = Dispatchers.getDefault();
        this.coroutineScope = CoroutineScopeKt.CoroutineScope(context);
    }

    @Override
    public List<NearestRestroomResponseDto> findNearest(double lat, double lon, int limit) {
        CompletableFuture<List<NearestRestroomResponseDto>> future = FutureKt.future(
            coroutineScope,
            (scope, continuation) -> restroomService.findNearestRestrooms(lat, lon, limit, null)
        );
        try {
            return future.get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to find nearest restrooms", e);
        }
    }

    @Override
    public Optional<RestroomResponseDto> getById(String id) {
        UUID uuid = UUID.fromString(id);
        CompletableFuture<RestroomResponseDto> future = FutureKt.future(
            coroutineScope,
            (scope, continuation) -> restroomService.getRestroomById(uuid)
        );
        try {
            RestroomResponseDto dto = future.get();
            return Optional.ofNullable(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get restroom by id", e);
        }
    }
}
