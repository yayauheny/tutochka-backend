package yayauheny.by.service.import.twogis

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.assertFailsWith
import org.junit.jupiter.api.Assertions.assertEquals
import yayauheny.by.service.import.InvalidImportPayload
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TwoGisScrapedParserTest {
    private val parser = TwoGisScrapedParser()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parse returns TwoGisScrapedPlace for valid scraped item`() {
        val item =
            buildJsonObject {
                put("id", "70000001062416076")
                put("title", "GreenTime, торгово-развлекательный центр")
                put("category", "mall")
                put("address", "Рудобельская, 3")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.861329)
                        put("lng", 27.638588)
                    }
                )
                put(
                    "attributeGroups",
                    json.parseToJsonElement("""["Торговые помещения", "Туалет"]""")
                )
                put("rubrics", json.parseToJsonElement("""["Торгово-развлекательные центры"]"""))
            }

        val result = parser.parse(item)

        assertEquals("70000001062416076", result.id)
        assertEquals("GreenTime, торгово-развлекательный центр", result.title)
        assertEquals("mall", result.category)
        assertEquals("Рудобельская, 3", result.address)
        assertEquals(53.861329, result.location.lat)
        assertEquals(27.638588, result.location.lng)
        assertEquals(listOf("Торговые помещения", "Туалет"), result.attributeGroups)
        assertEquals(listOf("Торгово-развлекательные центры"), result.rubrics)
    }

    @Test
    fun `parse uses empty string for null address`() {
        val item =
            buildJsonObject {
                put("id", "70000001088204422")
                put("title", "Лучшая улица, гастрономическое пространство")
                put("category", "food_restaurant")
                put("address", JsonNull)
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.927325)
                        put("lng", 27.61581)
                    }
                )
            }

        val result = parser.parse(item)

        assertEquals("70000001088204422", result.id)
        assertEquals("", result.address)
    }

    @Test
    fun `parse accepts null working_hours`() {
        val item =
            buildJsonObject {
                put("id", "123")
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

        val result = parser.parse(item)

        assertNull(result.workingHours)
    }

    @Test
    fun `parse throws for missing id`() {
        val item =
            buildJsonObject {
                put("title", "Test")
                put("address", "Test")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }

        assertFailsWith<InvalidImportPayload> {
            parser.parse(item)
        }
    }

    @Test
    fun `parse throws for missing location`() {
        val item =
            buildJsonObject {
                put("id", "123")
                put("title", "Test")
                put("address", "Test")
            }

        assertFailsWith<InvalidImportPayload> {
            parser.parse(item)
        }
    }

    @Test
    fun `parse handles empty attributeGroups`() {
        val item =
            buildJsonObject {
                put("id", "123")
                put("title", "Test")
                put("address", "Test")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
                put("attributeGroups", json.parseToJsonElement("[]"))
            }

        val result = parser.parse(item)

        assertEquals(emptyList<String>(), result.attributeGroups)
    }
}
