package yayauheny.by.unit.importing.provider.yandex

import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.loadImportResourceItems
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedParser

@DisplayName("YandexMapsScrapedParser Tests")
class YandexMapsScrapedParserTest {
    private val parser = YandexMapsScrapedParser()

    @Test
    fun `should parse minimal Yandex place`() {
        val givenJson =
            buildJsonObject {
                put("title", "Туалет")
                put("placeId", "yandex-123")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.56)
                    }
                )
            }

        val whenPlace = parser.parse(givenJson)

        assertEquals("Туалет", whenPlace.title)
        assertEquals("yandex-123", whenPlace.placeId)
        assertEquals(53.9, whenPlace.location.lat)
        assertEquals(27.56, whenPlace.location.lng)
    }

    @Test
    fun `should parse rich Yandex payload from resource dataset`() {
        val givenJson =
            loadImportResourceItems("import/yandex/yandex_scraped_places.json")
                .first { item -> item["placeId"]?.jsonPrimitive?.content == "137246000290" }

        val whenPlace = parser.parse(givenJson)
        val monday =
            whenPlace.workingHours!!
                .first { workingHour -> workingHour.day == "Mon" }

        assertEquals("Туалет", whenPlace.shortTitle)
        assertEquals("Минск, ул. Киселёва, 3", whenPlace.address)
        assertEquals("Закрыто до завтра", whenPlace.statusText)
        assertEquals(false, whenPlace.isOpenNow)
        assertEquals("Mon", monday.day)
        assertEquals(2, monday.intervals.size)
        assertEquals(listOf("недоступно"), whenPlace.features)
    }

    @Test
    fun `should parse accessible Yandex payload without working hours from resource dataset`() {
        val givenJson =
            loadImportResourceItems("import/yandex/yandex_scraped_places.json")
                .first { item -> item["placeId"]?.jsonPrimitive?.content == "88183159086" }

        val whenPlace = parser.parse(givenJson)

        assertEquals("Туалет", whenPlace.title)
        assertEquals("д. Боровляны, Малиновская ул., 2Б", whenPlace.address)
        assertNull(whenPlace.workingHours)
        assertEquals(
            listOf(
                "туалет для людей с инвалидностью",
                "пандус",
                "доступно",
                "парковка для людей с инвалидностью",
                "лифт"
            ),
            whenPlace.features
        )
    }

    @Test
    fun `should fail when placeId is missing`() {
        val givenJson =
            buildJsonObject {
                put("title", "Туалет")
                put(
                    "location",
                    buildJsonObject {
                        put("lat", 53.9)
                        put("lng", 27.56)
                    }
                )
            }

        assertFailsWith<InvalidImportPayload> {
            parser.parse(givenJson)
        }
    }
}
