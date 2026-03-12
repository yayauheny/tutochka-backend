package yayauheny.by.unit.service.import

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.service.import.ArrayOrSingleExtractor
import yayauheny.by.service.import.PayloadExtractor

@DisplayName("PayloadExtractor Tests")
class PayloadExtractorTest {
    private val extractor: PayloadExtractor = ArrayOrSingleExtractor()
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `should extract items from array payload`() {
        val payload =
            buildJsonObject {
                put(
                    "items",
                    buildJsonArray {
                        add(buildJsonObject { put("id", "1") })
                        add(buildJsonObject { put("id", "2") })
                        add(buildJsonObject { put("id", "3") })
                    }
                )
            }

        val items = extractor.extractItems(payload)

        assertEquals(3, items.size)
        assertEquals("1", items[0]["id"]?.toString()?.trim('"'))
        assertEquals("2", items[1]["id"]?.toString()?.trim('"'))
        assertEquals("3", items[2]["id"]?.toString()?.trim('"'))
    }

    @Test
    fun `should extract single object as list`() {
        val payload =
            buildJsonObject {
                put("id", "123")
                put("title", "Test")
            }

        val items = extractor.extractItems(payload)

        assertEquals(1, items.size)
        assertEquals("123", items[0]["id"]?.toString()?.trim('"'))
        assertEquals("Test", items[0]["title"]?.toString()?.trim('"'))
    }

    @Test
    fun `should handle empty items array`() {
        val payload =
            buildJsonObject {
                put("items", buildJsonArray { })
            }

        val items = extractor.extractItems(payload)

        assertTrue(items.isEmpty())
    }

    @Test
    fun `should filter out non-object elements from array`() {
        val payload =
            buildJsonObject {
                put(
                    "items",
                    buildJsonArray {
                        add(buildJsonObject { put("id", "1") })
                        add(JsonPrimitive("not an object"))
                        add(buildJsonObject { put("id", "2") })
                        add(JsonPrimitive(42))
                    }
                )
            }

        val items = extractor.extractItems(payload)

        assertEquals(2, items.size)
        assertEquals("1", items[0]["id"]?.toString()?.trim('"'))
        assertEquals("2", items[1]["id"]?.toString()?.trim('"'))
    }
}
