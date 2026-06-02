package yayauheny.by.bot.session

import org.springframework.stereotype.Component
import jakarta.annotation.PreDestroy
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

data class UserSession(
    val location: Location?,
    val updatedAt: Instant = Instant.now()
) {
    data class Location(
        val latitude: Double,
        val longitude: Double
    )

    companion object {
        fun withLocation(location: Location): UserSession {
            return UserSession(location)
        }
    }

    fun updateLocation(newLocation: Location): UserSession {
        return copy(location = newLocation, updatedAt = Instant.now())
    }
}

interface SessionStore {
    fun get(userId: Long): Optional<UserSession>
    fun put(userId: Long, session: UserSession)
    fun remove(userId: Long)
}

@Component
class InMemorySessionStore : SessionStore {
    private val sessions = ConcurrentHashMap<Long, UserSession>()
    private val cleanupExecutor = Executors.newSingleThreadScheduledExecutor()

    companion object {
        private const val TTL_MINUTES = 10L
    }

    init {
        cleanupExecutor.scheduleAtFixedRate(this::cleanupExpiredSessions, 5, 5, TimeUnit.MINUTES)
    }

    @PreDestroy
    fun shutdown() {
        cleanupExecutor.shutdown()
    }

    override fun get(userId: Long): Optional<UserSession> {
        val session = sessions[userId] ?: return Optional.empty()

        if (isExpired(session)) {
            sessions.remove(userId)
            return Optional.empty()
        }

        return Optional.of(session)
    }

    override fun put(userId: Long, session: UserSession) {
        sessions[userId] = session
    }

    override fun remove(userId: Long) {
        sessions.remove(userId)
    }

    private fun isExpired(session: UserSession): Boolean {
        return session.updatedAt.isBefore(Instant.now().minus(TTL_MINUTES, ChronoUnit.MINUTES))
    }

    private fun cleanupExpiredSessions() {
        val now = Instant.now()
        sessions.entries.removeIf { it.value.updatedAt.isBefore(now.minus(TTL_MINUTES, ChronoUnit.MINUTES)) }
    }
}
