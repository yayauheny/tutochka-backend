package yayauheny.by.unit.importing.dedup

import kotlin.test.assertEquals
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.dedup.PayloadHashing

@DisplayName("PayloadHashing Tests")
class PayloadHashingTest {
    @Test
    fun `canonical JSON hash is stable across field order`() {
        val first =
            buildJsonObject {
                put("id", "123")
                put("title", "Toilet")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.5)
                    }
                )
            }
        val second =
            buildJsonObject {
                put(
                    "location",
                    buildJsonObject {
                        put("lng", 27.5)
                        put("lat", 53.9)
                    }
                )
                put("title", "Toilet")
                put("id", "123")
            }

        assertEquals(
            PayloadHashing.canonicalPayloadHash(first),
            PayloadHashing.canonicalPayloadHash(second)
        )
    }
}
