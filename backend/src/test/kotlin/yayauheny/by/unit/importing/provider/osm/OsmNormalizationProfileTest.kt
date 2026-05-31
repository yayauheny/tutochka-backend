package yayauheny.by.unit.importing.provider.osm

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.provider.osm.OsmNormalizationProfile
import yayauheny.by.importing.provider.osm.OsmScrapedParser
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.testfixtures.OsmOriginalRows
import kotlinx.serialization.json.toJacksonNode

@DisplayName("OsmNormalizationProfile Tests")
class OsmNormalizationProfileTest {
    private val parser = OsmScrapedParser()

    @Test
    fun `given missing name when resolve then default toilet name is used`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.bareAmenityOnly().toJacksonNode()))

        assertEquals("Туалет", resolved.name)
    }

    @Test
    fun `given address tags when resolve then full address is assembled`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.addressTaggedPaid().toJacksonNode()))

        assertEquals("праезд Чыжэўскіх, 13", resolved.address)
        assertNull(resolved.buildingContext)
    }

    @Test
    fun `given pit latrine row when resolve then standalone location is preserved`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.pitlatrine().toJacksonNode()))

        assertEquals(LocationType.STANDALONE, resolved.locationType)
        assertEquals(PlaceType.PUBLIC, resolved.placeType)
        assertEquals(GenderType.UNISEX, resolved.genderType)
        assertEquals("pitlatrine", resolved.amenities["disposal"]?.asText())
    }

    @Test
    fun `given original female free row when resolve then fee and gender are preserved`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.femaleFree().toJacksonNode()))

        assertEquals(FeeType.FREE, resolved.feeType)
        assertEquals(GenderType.WOMEN, resolved.genderType)
    }

    @Test
    fun `given original wheelchair row when resolve then accessibility is preserved`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.accessCustomersWheelchair().toJacksonNode()))

        assertEquals(AccessibilityType.WHEELCHAIR, resolved.accessibilityType)
        assertEquals(
            true,
            resolved.amenities["access_limited"]?.asText()?.toBoolean()
        )
    }

    @Test
    fun `given original indoor row with address when resolve then building context is created`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.indoorWithAddress().toJacksonNode()))

        assertEquals(LocationType.INSIDE_BUILDING, resolved.locationType)
        assertEquals(PlaceType.OTHER, resolved.placeType)
        assertNotNull(resolved.buildingContext)
        assertEquals("Татарская вуліца, 2", resolved.buildingContext!!.address)
        assertNull(resolved.buildingContext!!.name)
    }

    @Test
    fun `given building toilets row from original json when resolve then standalone location is preserved`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.buildingToiletsOnly().toJacksonNode()))

        assertEquals("Надворный туалет (служебный)", resolved.name)
        assertEquals(LocationType.STANDALONE, resolved.locationType)
        assertEquals(PlaceType.PUBLIC, resolved.placeType)
        assertNull(resolved.buildingContext)
    }

    @Test
    fun `given original indoor row without address when resolve then location stays unknown`() {
        val resolved = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.indoorPaidWheelchair().toJacksonNode()))

        assertEquals(LocationType.UNKNOWN, resolved.locationType)
        assertNull(resolved.buildingContext)
    }

    @Test
    fun `given two original rows from same building when resolve then building external id is stable`() {
        val first = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.sameBuildingLevelOne().toJacksonNode()))
        val second = OsmNormalizationProfile.resolve(parser.parse(OsmOriginalRows.sameBuildingLevelTwo().toJacksonNode()))

        assertNotNull(first.buildingContext)
        assertNotNull(second.buildingContext)
        assertEquals(LocationType.INSIDE_BUILDING, first.locationType)
        assertEquals(LocationType.INSIDE_BUILDING, second.locationType)
        assertEquals("Першамайская вуліца, 57", first.buildingContext!!.address)
        assertEquals(first.buildingContext!!.externalId, second.buildingContext!!.externalId)
    }
}
