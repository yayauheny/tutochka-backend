package by.yayauheny.tutochkatgbot.service;

import by.yayauheny.tutochkatgbot.session.SessionStore;
import by.yayauheny.tutochkatgbot.session.UserSession;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for user preferences and session management
 */
@Service
public class UserService {
    private final SessionStore sessionStore;
    public static final int DEFAULT_RADIUS = 500;

    public UserService(SessionStore sessionStore) {
        this.sessionStore = sessionStore;
    }

    /**
     * Save user location
     * @param userId user ID
     * @param latitude latitude
     * @param longitude longitude
     */
    public void saveLocation(long userId, double latitude, double longitude) {
        UserSession.Location location = new UserSession.Location(latitude, longitude);
        UserSession currentSession = sessionStore.get(userId)
            .orElse(UserSession.withLocation(location, DEFAULT_RADIUS));
        
        UserSession updatedSession = currentSession.updateLocation(location);
        sessionStore.put(userId, updatedSession);
    }

    /**
     * Get user session
     * @param userId user ID
     * @return user session
     */
    public Optional<UserSession> getSession(long userId) {
        return sessionStore.get(userId);
    }

    /**
     * Clear user session
     * @param userId user ID
     */
    public void clearSession(long userId) {
        sessionStore.remove(userId);
    }
}
