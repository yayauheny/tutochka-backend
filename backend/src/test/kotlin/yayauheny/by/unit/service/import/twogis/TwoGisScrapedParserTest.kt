package yayauheny.by.unit.service.import.twogis

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.service.import.twogis.TwoGisScrapedParser

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

        assertFailsWith<IllegalArgumentException> {
            parser.parse(json)
        }
    }

    @Test
    fun `should fail on missing required field title`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("address", "Test Street")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        assertFailsWith<IllegalArgumentException> {
            parser.parse(json)
        }
    }

    @Test
    fun `should return empty address when address is missing`() {
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
        assertEquals("", place.address)
    }

    @Test
    fun `should fail on missing required field location`() {
        val json =
            buildJsonObject {
                put("id", "12345")
                put("title", "Test")
                put("address", "Test Street")
            }

        assertFailsWith<IllegalArgumentException> {
            parser.parse(json)
        }
    }
}
