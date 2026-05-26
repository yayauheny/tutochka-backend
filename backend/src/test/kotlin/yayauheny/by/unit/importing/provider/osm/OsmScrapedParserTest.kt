package yayauheny.by.unit.importing.provider.osm

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.osm.OsmScrapedParser
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.testfixtures.OsmOriginalRows

@DisplayName("OsmScrapedParser Tests")
class OsmScrapedParserTest {
    private val parser = OsmScrapedParser()

    @Test
    fun `given web mercator geom when parse then coordinates are converted to wgs84`() {
        val payload = OsmOriginalRows.accessCustomersWheelchair()

        val parsed = parser.parse(payload)

        assertEquals("4115239922", parsed.nodeId)
        assertEquals("customers", parsed.tags["access"])
        assertTrue(parsed.location.lat in 55.0..55.5)
        assertTrue(parsed.location.lng in 30.0..30.5)
    }

    @Test
    fun `given unusable access when parse then payload is rejected`() {
        val payload = OsmOriginalRows.accessPrivateChemical()

        assertFailsWith<InvalidImportPayload> {
            parser.parse(payload)
        }
    }

    @Test
    fun `given wc name from original json when parse then payload is accepted`() {
        val parsed = parser.parse(OsmOriginalRows.wcNameOnly())

        assertEquals("5925889233", parsed.nodeId)
        assertEquals("WC", parsed.tags["name"])
    }

    @Test
    fun `given building toilets row from original json when parse then payload is accepted`() {
        val parsed = parser.parse(OsmOriginalRows.buildingToiletsOnly())

        assertEquals("12960751816", parsed.nodeId)
        assertEquals("toilets", parsed.tags["building"])
    }

    @Test
    fun `given description matched row from original json when parse then payload is accepted`() {
        val parsed = parser.parse(OsmOriginalRows.descriptionMatchedCampSite())

        assertEquals("3664477258", parsed.nodeId)
        assertNotNull(parsed.tags["description"])
    }

    @Test
    fun `given valid payload when convert then normalized restroom candidate is returned`() {
        val payload = OsmOriginalRows.femaleFree()

        val candidate = parser.convert(payload, ImportAdapterContext(ImportPayloadType.OSM_TOILET_JSON, UUID.randomUUID()))

        assertEquals("4865147166", candidate.providerObjectId)
        assertEquals("Туалет", candidate.name)
        assertEquals(FeeType.FREE, candidate.feeType)
    }
}
