package yayauheny.by.unit.importing.provider

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.exception.UnsupportedPayloadType
import yayauheny.by.importing.provider.ImportCapabilities
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

@DisplayName("ImportCapabilities Tests")
class ImportCapabilitiesTest {
    @Test
    @DisplayName("requireSupported does not throw for allowed provider and payloadType")
    fun requireSupported_does_not_throw_for_allowed_pair() {
        ImportCapabilities.requireSupported(
            ImportProvider.TWO_GIS,
            ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON
        )
    }

    @Test
    @DisplayName("requireSupported throws UnsupportedPayloadType for disallowed payloadType")
    fun requireSupported_throws_for_disallowed_payload_type() {
        val e =
            assertFailsWith<UnsupportedPayloadType> {
                ImportCapabilities.requireSupported(
                    ImportProvider.TWO_GIS,
                    ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON
                )
            }
        assertEquals(ImportProvider.TWO_GIS, e.provider)
        assertEquals(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON, e.payloadType)
        assertTrue(e.message!!.contains("TWO_GIS") && e.message!!.contains("YANDEX_MAPS_SCRAPED_PLACE_JSON"))
    }

    @Test
    @DisplayName("requireSupported throws UnsupportedPayloadType for unknown provider")
    fun requireSupported_throws_for_unknown_provider() {
        assertFailsWith<UnsupportedPayloadType> {
            ImportCapabilities.requireSupported(
                ImportProvider.YANDEX_MAPS,
                ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON
            )
        }
    }
}
