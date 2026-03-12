package by.yayauheny.tutochkatgbot.session;

import java.util.Optional;

/**
 * Interface for session storage
 */
public interface SessionStore {
    Optional<UserSession> get(long userId);
    void put(long userId, UserSession session);
    void remove(long userId);
}
