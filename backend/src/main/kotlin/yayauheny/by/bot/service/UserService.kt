package yayauheny.by.bot.service

import org.springframework.stereotype.Service
import yayauheny.by.bot.session.SessionStore
import yayauheny.by.bot.session.UserSession
import java.util.Optional

@Service
class UserService(
    private val sessionStore: SessionStore
) {
    companion object {
        const val DEFAULT_RADIUS = 500
    }

    fun saveLocation(userId: Long, latitude: Double, longitude: Double) {
        val location = UserSession.Location(latitude, longitude)
        val currentSession = sessionStore.get(userId)
            .orElseGet { UserSession.withLocation(location, DEFAULT_RADIUS) }

        val updatedSession = currentSession.updateLocation(location)
        sessionStore.put(userId, updatedSession)
    }

    fun getSession(userId: Long): Optional<UserSession> {
        return sessionStore.get(userId)
    }

    fun clearSession(userId: Long) {
        sessionStore.remove(userId)
    }
}
