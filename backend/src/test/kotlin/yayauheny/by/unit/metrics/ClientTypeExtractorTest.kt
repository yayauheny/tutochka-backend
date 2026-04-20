package yayauheny.by.unit.metrics

import io.ktor.http.headersOf
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.ApplicationRequest
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.jupiter.api.Test
import yayauheny.by.metrics.extractClientType

class ClientTypeExtractorTest {
    @Test
    fun `extractClientType should read telegram_bot from header`() {
        val call = mockCallWithClientType("telegram_bot")
        assertEquals("telegram_bot", call.extractClientType())
    }

    @Test
    fun `extractClientType should fallback to api for unsupported value`() {
        val call = mockCallWithClientType("mobile_app")
        assertEquals("api", call.extractClientType())
    }

    @Test
    fun `extractClientType should fallback to api when header is missing`() {
        val call = mockCallWithClientType(null)
        assertEquals("api", call.extractClientType())
    }

    private fun mockCallWithClientType(clientType: String?): ApplicationCall {
        val call = mockk<ApplicationCall>()
        val request = mockk<ApplicationRequest>()
        every { call.request } returns request
        every {
            request.headers
        } returns
            if (clientType == null) {
                headersOf()
            } else {
                headersOf("X-Client-Type", clientType)
            }
        return call
    }
}
