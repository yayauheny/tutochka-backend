package yayauheny.by.unit.importing.provider.twogis

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.twogis.TwoGisScrapedParser

@DisplayName("TwoGisScrapedParser Tests")
class TwoGisScrapedParserTest {
    private val parser = TwoGisScrapedParser()

    @Test
    fun `should parse complete scraped place`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test Toilet")
                put("category", "toilet")
                put("address", "Test Street 1")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
                put(
                    "working_hours",
                    buildJsonObject {
                        put(
                            "Mon",
                            buildJsonObject {
                                put(
                                    "working_hours",
                                    buildJsonArray {
                                        add(
                                            buildJsonObject {
                                                put("from", "08:00")
                                                put("to", "22:00")
                                            }
                                        )
                                    }
                                )
                            }
                        )
                    }
                )
                put(
                    "attributeGroups",
                    buildJsonArray {
                        add(JsonPrimitive("платный туалет"))
                        add(JsonPrimitive("оплата картой"))
                    }
                )
                put(
                    "rubrics",
                    buildJsonArray {
                        add(JsonPrimitive("toilet"))
                    }
                )
            }

        val place = parser.parse(json)

        assertEquals("12345", place.id)
        assertEquals("Test Toilet", place.title)
        assertEquals("toilet", place.category)
        assertEquals("Test Street 1", place.address)
        assertEquals(53.9, place.location.lat)
        assertEquals(27.5, place.location.lng)
        assertEquals(2, place.attributeGroups.size)
        assertEquals("платный туалет", place.attributeGroups[0])
        assertEquals(1, place.rubrics.size)
        assertNotNull(place.workingHours)
    }

    @Test
    fun `should parse place with minimal fields`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("address", "Test Street")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)

        assertEquals("12345", place.id)
        assertEquals("Test", place.title)
        assertEquals("Test Street", place.address)
        assertNull(place.category)
        assertNull(place.workingHours)
        assertTrue(place.attributeGroups.isEmpty())
        assertTrue(place.rubrics.isEmpty())
    }

    @Test
    fun `should fail on missing required field id`() {
        val json =
            buildJsonObject {
                put("title", "Test")
                put("address", "Test Street")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        assertFailsWith<InvalidImportPayload> {
            parser.parse(json)
        }
    }

    @Test
    fun `should preserve missing title as null`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("address", "Test Street")
                put(
                    "rubrics",
                    buildJsonArray { add(JsonPrimitive("Туалеты")) }
                )
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)
        assertNull(place.title)
    }

    @Test
    fun `should preserve missing title and address fields as null`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)
        assertNull(place.title)
        assertNull(place.address)
    }

    @Test
    fun `should preserve missing address street houseNumber as null`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)
        assertNull(place.address)
        assertNull(place.street)
        assertNull(place.houseNumber)
    }

    @Test
    fun `should preserve street and houseNumber when address is missing`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("street", "Рудобельская")
                put("houseNumber", "3")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)
        assertNull(place.address)
        assertEquals("Рудобельская", place.street)
        assertEquals("3", place.houseNumber)
    }

    @Test
    fun `should fail on missing required field location`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("address", "Test Street")
            }

        assertFailsWith<InvalidImportPayload> {
            parser.parse(json)
        }
    }

    @Test
    fun `should preserve null address`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("address", JsonNull)
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        val place = parser.parse(json)
        assertNull(place.address)
    }

    @Test
    fun `should accept null working_hours`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("address", "Test address")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
                put("working_hours", JsonNull)
            }

        val place = parser.parse(json)
        assertNull(place.workingHours)
    }
}
