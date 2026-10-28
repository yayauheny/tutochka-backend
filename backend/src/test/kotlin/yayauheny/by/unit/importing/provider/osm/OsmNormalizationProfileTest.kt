package yayauheny.by.unit.importing.provider.osm

import java.util.UUID
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.provider.osm.OsmNormalizationProfile
import yayauheny.by.importing.provider.osm.OsmScrapedParser
import yayauheny.by.importing.mapper.RestroomImportMapper
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.model.enums.ImportPayloadType
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
    private val objectMapper = jacksonObjectMapper()

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

    @Test
    fun `given reference csv when reading keys then snapshot matches expected set`() {
        val actualKeys = readReferenceCsvKeys()

        assertEquals(expectedCsvKeys, actualKeys)
    }

    @Test
    fun `given rich osm row when normalize then phones access note and raw tags are preserved`() {
        val payload =
            OsmOriginalRows.withTags(
                OsmOriginalRows.bareAmenityOnly(),
                mapOf(
                    "phone" to "+375291112233",
                    "contact:phone" to "+375441112233",
                    "description" to "Use side entrance",
                    "designation" to "Customer toilets",
                    "access" to "customers",
                    "wheelchair" to "limited",
                    "website" to "https://example.com",
                    "payment:cash" to "yes"
                )
            )

        val candidate = parser.convert(payload.toJacksonNode(), ImportAdapterContext(ImportPayloadType.OSM_TOILET_JSON, UUID.randomUUID()))
        val resolved = OsmNormalizationProfile.resolve(parser.parse(payload.toJacksonNode()))
        val dto = RestroomImportMapper.toCreateDto(candidate)

        assertEquals("+375291112233", candidate.phones?.get("phone"))
        assertEquals("+375441112233", candidate.phones?.get("contact:phone"))
        assertEquals("Use side entrance", candidate.accessNote)
        assertEquals("Use side entrance", dto.accessNote)

        val osmTags = resolved.amenities["osm_tags"]
        assertNotNull(osmTags)
        assertEquals("+375291112233", osmTags["phone"].asText())
        assertEquals("+375441112233", osmTags["contact:phone"].asText())
        assertEquals("Use side entrance", osmTags["description"].asText())
        assertEquals("Customer toilets", osmTags["designation"].asText())
        assertEquals("https://example.com", osmTags["website"].asText())

        @Suppress("UNCHECKED_CAST")
        val dtoOsmTags = dto.amenities["osm_tags"] as Map<String, Any>
        assertEquals("+375291112233", dtoOsmTags["phone"])
        assertEquals("+375441112233", dtoOsmTags["contact:phone"])
        assertEquals("Use side entrance", dtoOsmTags["description"])
        assertEquals("https://example.com", dtoOsmTags["website"])
    }

    @Test
    fun `blank address and opening hours are normalized consistently`() {
        val payload =
            OsmOriginalRows.withTags(
                OsmOriginalRows.bareAmenityOnly(),
                mapOf(
                    "addr:full" to " ",
                    "addr:street" to " ",
                    "addr:housenumber" to " ",
                    "opening_hours" to " null "
                )
            )

        val resolved = OsmNormalizationProfile.resolve(parser.parse(payload.toJacksonNode()))

        assertNull(resolved.address)
        assertNull(resolved.rawSchedule)
    }

    private fun readReferenceCsvKeys(): Set<String> {
        val resource =
            requireNotNull(
                javaClass.classLoader.getResourceAsStream(
                    "db/changelog/reference_data/restrooms_osm.csv"
                )
            ) {
                "Reference CSV not found"
            }

        val keys = linkedSetOf<String>()
        resource.bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.drop(1).forEach { line ->
                if (line.isBlank()) {
                    return@forEach
                }

                val match = CSV_ROW_REGEX.matchEntire(line)
                    ?: error("Unexpected CSV row: $line")
                val tagsJson = match.groupValues[2].replace("\"\"", "\"")
                val tags = objectMapper.readTree(tagsJson)
                val fields = tags.fieldNames()
                while (fields.hasNext()) {
                    keys += fields.next()
                }
            }
        }
        return keys
    }

    private companion object {
        val CSV_ROW_REGEX = Regex("""^([0-9]+),"((?:[^"]|"")*)",(.*)$""")
        val expectedCsvKeys = setOf(
            "abandoned",
            "access",
            "addr:city",
            "addr:city:ru",
            "addr:country",
            "addr:floor",
            "addr:full",
            "addr:housenumber",
            "addr:milestone",
            "addr:postcode",
            "addr:street",
            "amenity",
            "building",
            "building:levels",
            "capacity",
            "changing_table",
            "changing_table:fee",
            "changing_table:location",
            "charge",
            "check_date",
            "check_date:opening_hours",
            "check_date:wheelchair",
            "contact:phone",
            "description",
            "designation",
            "drinking_water",
            "email",
            "entrance",
            "fee",
            "female",
            "image",
            "indoor",
            "internet_access",
            "internet_access:fee",
            "layer",
            "level",
            "location",
            "male",
            "mapillary",
            "name",
            "name:be",
            "name:ru",
            "opening_hours",
            "operator",
            "operator:type",
            "operator:wikidata",
            "outdoor_seating",
            "payment:cash",
            "payment:coins",
            "payment:contactless",
            "payment:credit_cards",
            "payment:debit_cards",
            "payment:notes",
            "phone",
            "portable",
            "power_supply",
            "sanitary_dump_station",
            "shop",
            "shower",
            "smoking",
            "supervised",
            "toilets",
            "toilets:disposal",
            "toilets:handwashing",
            "toilets:menstrual_products",
            "toilets:number",
            "toilets:paper_supplied",
            "toilets:position",
            "toilets:type",
            "toll",
            "tourism",
            "unisex",
            "website",
            "wheelchair"
        )
    }
}
