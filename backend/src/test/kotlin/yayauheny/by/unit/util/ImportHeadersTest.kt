package yayauheny.by.unit.util

import io.ktor.http.Headers
import io.ktor.http.headersOf
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Test
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.util.getImportHeaders

class ImportHeadersTest {
    @Test
    fun `getImportHeaders should parse valid headers`() {
        val headers =
            Headers.build {
                append("Import-Provider", "YANDEX_MAPS")
                append("Import-Payload-Type", "YANDEX_MAPS_SCRAPED_PLACE_JSON")
                append("Import-City-Id", "550e8400-e29b-41d4-a716-446655440000")
            }

        val importHeaders = headers.getImportHeaders()

        assertEquals(ImportProvider.YANDEX_MAPS, importHeaders.provider)
        assertEquals(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON, importHeaders.payloadType)
        assertEquals("550e8400-e29b-41d4-a716-446655440000", importHeaders.cityId.toString())
    }

    @Test
    fun `getImportHeaders should fail when required header is missing`() {
        val headers = headersOf("Import-Payload-Type", "YANDEX_MAPS_SCRAPED_PLACE_JSON")

        assertFailsWith<ValidationException> {
            headers.getImportHeaders()
        }
    }
}
