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

        service.saveLocation(42L, 53.9, 27.56)

        verify { sessionStore.put(42L, capture(sessionSlot)) }
        assertThat(sessionSlot.captured.location).isEqualTo(UserSession.Location(53.9, 27.56))
    }

    @Test
    fun `saveLocation updates existing session`() {
        val existing = UserSession.withLocation(UserSession.Location(1.0, 2.0))
        every { sessionStore.get(42L) } returns Optional.of(existing)
        val sessionSlot = slot<UserSession>()

        service.saveLocation(42L, 53.9, 27.56)

        verify { sessionStore.put(42L, capture(sessionSlot)) }
        assertThat(sessionSlot.captured.location).isEqualTo(UserSession.Location(53.9, 27.56))
    }

    @Test
    fun `getSession returns store value`() {
        val session = UserSession.withLocation(UserSession.Location(53.9, 27.56))
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
