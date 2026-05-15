package yayauheny.by.unit.importing.provider

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.twogis.TwoGisImportAdapter
import yayauheny.by.importing.provider.yandex.YandexImportAdapter
import yayauheny.by.model.enums.ImportPayloadType

@DisplayName("ImportAdapter Metadata Tests")
class ImportAdapterMetadataTest {
    @Test
    fun `TwoGis adapter extracts inbox metadata from 2gis payload`() {
        val adapter = TwoGisImportAdapter()
        val payload =
            buildJsonObject {
                put("id", "2gis-123")
                put("title", "Test Toilet")
                put("address", "Street 1")
                put("url", "https://2gis.by/minsk/firm/2gis-123")
                put("scrapedAt", "2025-12-24T09:25:00.392Z")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val envelope = adapter.parseEnvelope(payload, ImportAdapterContext(ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON, UUID.randomUUID()))

        assertEquals("2gis-123", envelope.inboxMetadata.externalId)
        assertEquals("https://2gis.by/minsk/firm/2gis-123", envelope.inboxMetadata.sourceUrl)
        assertNotNull(envelope.inboxMetadata.scrapedAt)
    }

    @Test
    fun `TwoGis adapter extracts source country and city`() {
        val adapter = TwoGisImportAdapter()
        val payload =
            buildJsonObject {
                put("country", "Республика Беларусь")
                put("city", "Минск")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val sourceLocation = adapter.extractSourceLocation(payload)

        assertEquals("Республика Беларусь", sourceLocation.country)
        assertEquals("Минск", sourceLocation.city)
        assertEquals(53.9, sourceLocation.lat)
        assertEquals(27.5, sourceLocation.lng)
    }

    @Test
    fun `Yandex adapter extracts inbox metadata from Yandex payload`() {
        val adapter = YandexImportAdapter()
        val payload =
            buildJsonObject {
                put("title", "Туалет")
                put("placeId", "yandex-123")
                put("yandexUri", "yandexmaps://place/yandex-123")
                put("scrapedAt", "2025-12-24T09:25:00.392Z")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.56)
                    }
                )
            }

        val envelope =
            adapter.parseEnvelope(
                payload,
                ImportAdapterContext(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON, UUID.randomUUID())
            )

        assertEquals("yandex-123", envelope.inboxMetadata.externalId)
        assertEquals("yandexmaps://place/yandex-123", envelope.inboxMetadata.sourceUrl)
        assertNotNull(envelope.inboxMetadata.scrapedAt)
    }

    @Test
    fun `Yandex adapter extracts canonical city from address when state is region`() {
        val adapter = YandexImportAdapter()
        val payload =
            buildJsonObject {
                put("address", "д. Боровляны, Малиновская ул., 2Б")
                put("state", "Минская область")
                put("country", "Беларусь")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 54.002693)
                        put("lng", 27.660602)
                    }
                )
            }

        val sourceLocation = adapter.extractSourceLocation(payload)

        assertEquals("Беларусь", sourceLocation.country)
        assertEquals("Боровляны", sourceLocation.city)
        assertEquals(54.002693, sourceLocation.lat)
        assertEquals(27.660602, sourceLocation.lng)
    }

    @Test
    fun `Yandex adapter leaves city empty when state is region and address has no locality`() {
        val adapter = YandexImportAdapter()
        val payload =
            buildJsonObject {
                put("address", "Минская область, трасса М3")
                put("state", "Минская область")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 54.0)
                        put("lng", 27.6)
                    }
                )
            }

        val sourceLocation = adapter.extractSourceLocation(payload)

        assertNull(sourceLocation.city)
    }
}
