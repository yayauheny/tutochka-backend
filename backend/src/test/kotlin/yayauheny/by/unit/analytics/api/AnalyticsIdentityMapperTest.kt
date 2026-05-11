package yayauheny.by.unit.analytics.api

import io.ktor.http.Headers
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.Test
import yayauheny.by.analytics.api.getAnalyticsIdentityHeaders
import yayauheny.by.model.analytics.AnalyticsSource

class AnalyticsIdentityMapperTest {
    @Test
    fun `getAnalyticsIdentityHeaders should map analytics headers`() {
        val headers =
            Headers.build {
                append("X-Tg-User-Id", "12345")
                append("X-Tg-Chat-Id", "chat-1")
                append("X-Tg-Username", " user ")
                append("X-Analytics-Source", "api")
            }

        val identity = headers.getAnalyticsIdentityHeaders(AnalyticsSource.TELEGRAM_BOT)

        assertEquals(12345L, identity.tgUserId)
        assertEquals("chat-1", identity.tgChatId)
        assertEquals("user", identity.username)
        assertEquals(AnalyticsSource.API, identity.source)
    }

    @Test
    fun `getAnalyticsIdentityHeaders should fallback to default source for invalid header`() {
        val headers =
            Headers.build {
                append("X-Tg-User-Id", "not-a-number")
                append("X-Analytics-Source", "unknown")
            }

        val identity = headers.getAnalyticsIdentityHeaders(AnalyticsSource.TELEGRAM_BOT)

        assertNull(identity.tgUserId)
        assertEquals(AnalyticsSource.TELEGRAM_BOT, identity.source)
    }
}
