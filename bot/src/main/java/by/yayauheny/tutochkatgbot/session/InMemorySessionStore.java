package by.yayauheny.tutochkatgbot.session;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PreDestroy;

/**
 * In-memory session store with TTL cleanup
 */
@Component
public class InMemorySessionStore implements SessionStore {
    
    private static final int TTL_MINUTES = 10;
    private final ConcurrentHashMap<Long, UserSession> sessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
    
    public InMemorySessionStore() {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES);
    }
    
    @PreDestroy
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
    
    @Override
    public Optional<UserSession> get(long userId) {
        UserSession session = sessions.get(userId);
        if (session == null) {
            return Optional.empty();
        }
        
        if (isExpired(session)) {
            sessions.remove(userId);
            return Optional.empty();
        }
        
        return Optional.of(session);
    }
    
    @Override
    public void put(long userId, UserSession session) {
        sessions.put(userId, session);
    }
    
    @Override
    public void remove(long userId) {
        sessions.remove(userId);
    }
    
    private boolean isExpired(UserSession session) {
        return session.updatedAt().isBefore(Instant.now().minus(TTL_MINUTES, ChronoUnit.MINUTES));
    }
    
    private void cleanupExpiredSessions() {
        sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }
}
