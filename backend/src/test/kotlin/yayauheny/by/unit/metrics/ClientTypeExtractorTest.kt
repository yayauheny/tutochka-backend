package yayauheny.by.unit.metrics

import io.ktor.http.headersOf
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import yayauheny.by.metrics.extractClientType

class ClientTypeExtractorTest {
    @Test
    fun `extractClientType should read telegram_bot from header`() {
        val headers = headersWithClientType("telegram_bot")
        assertEquals("telegram_bot", headers.extractClientType())
    }

    @Test
    fun `extractClientType should fallback to api for unsupported value`() {
        val headers = headersWithClientType("mobile_app")
        assertEquals("api", headers.extractClientType())
    }

    @Test
    fun `extractClientType should fallback to api when header is missing`() {
        val headers = headersWithClientType(null)
        assertEquals("api", headers.extractClientType())
    }

    private fun headersWithClientType(clientType: String?) =
        if (clientType == null) {
            headersOf()
        } else {
            headersOf("X-Client-Type", clientType)
        }
}
