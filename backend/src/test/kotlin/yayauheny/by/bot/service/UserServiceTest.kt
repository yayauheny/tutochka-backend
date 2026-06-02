package yayauheny.by.bot.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import java.util.Optional
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import yayauheny.by.bot.session.SessionStore
import yayauheny.by.bot.session.UserSession

class UserServiceTest {
    private val sessionStore = mockk<SessionStore>(relaxed = true)
    private val service = UserService(sessionStore)

    @Test
    fun `saveLocation creates new session with default radius when absent`() {
        every { sessionStore.get(42L) } returns Optional.empty()
        val sessionSlot = slot<UserSession>()

        val result = service.saveLocation(42L, 53.9, 27.56)

        verify { sessionStore.put(42L, capture(sessionSlot)) }
        assertThat(sessionSlot.captured.location).isEqualTo(UserSession.Location(53.9, 27.56))
        assertThat(sessionSlot.captured.radius).isEqualTo(UserService.DEFAULT_RADIUS)
        assertThat(result).isEqualTo(sessionSlot.captured)
    }

    @Test
    fun `saveLocation updates existing session while preserving radius`() {
        val existing = UserSession.withLocation(UserSession.Location(1.0, 2.0), 900)
        every { sessionStore.get(42L) } returns Optional.of(existing)
        val sessionSlot = slot<UserSession>()

        val result = service.saveLocation(42L, 53.9, 27.56)

        verify { sessionStore.put(42L, capture(sessionSlot)) }
        assertThat(sessionSlot.captured.location).isEqualTo(UserSession.Location(53.9, 27.56))
        assertThat(sessionSlot.captured.radius).isEqualTo(900)
        assertThat(result).isEqualTo(sessionSlot.captured)
    }

    @Test
    fun `updateRadius preserves location and stores selected radius`() {
        val existing = UserSession.withLocation(UserSession.Location(53.9, 27.56), 500)
        every { sessionStore.get(42L) } returns Optional.of(existing)
        val sessionSlot = slot<UserSession>()

        val result = service.updateRadius(42L, 2000)

        verify { sessionStore.put(42L, capture(sessionSlot)) }
        assertThat(sessionSlot.captured.location).isEqualTo(UserSession.Location(53.9, 27.56))
        assertThat(sessionSlot.captured.radius).isEqualTo(2000)
        assertThat(result).isEqualTo(sessionSlot.captured)
    }

    @Test
    fun `getSession returns store value`() {
        val session = UserSession.withLocation(UserSession.Location(53.9, 27.56), 500)
        every { sessionStore.get(42L) } returns Optional.of(session)

        val result = service.getSession(42L)

        assertThat(result).contains(session)
    }

    @Test
    fun `clearSession removes user session`() {
        service.clearSession(42L)

        verify { sessionStore.remove(42L) }
    }
}
