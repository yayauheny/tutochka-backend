package yayauheny.by.unit.importing.provider.twogis

import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.loadImportResourceItems
import yayauheny.by.importing.provider.twogis.TwoGisScrapedParser

@DisplayName("TwoGisScrapedParser Dataset Tests")
class TwoGisScrapedParserDatasetTest {
    private val parser = TwoGisScrapedParser()

    @Test
    fun `should parse rich 2gis payload from resource dataset`() {
        val givenJson =
            loadImportResourceItems("import/2gis/2gis_scraped_places.json")
                .first { item -> item["id"]?.jsonPrimitive?.content == "70000001042444951" }

        val whenPlace = parser.parse(givenJson)

        assertEquals("Dana Mall, торгово-развлекательный комплекс", whenPlace.title)
        assertEquals("mall_floor", whenPlace.category)
        assertEquals("Петра Мстиславца, 11", whenPlace.address)
        assertEquals("Петра Мстиславца", whenPlace.street)
        assertEquals("11", whenPlace.houseNumber)
        assertEquals(
            "09:00",
            whenPlace.workingHours!!["Mon"]!!
                .jsonObject["from"]!!
                .jsonPrimitive.content
        )
        assertEquals(
            listOf(
                "Детская игровая комната / площадка",
                "Бесплатный Wi-Fi",
                "Туалет",
                "Пандус",
                "Широкий лифт",
                "Туалет для маломобильных людей",
                "Автоматическая дверь",
                "Доступный вход для людей с инвалидностью"
            ),
            whenPlace.attributeGroups
        )
        assertEquals(listOf("Торгово-развлекательные центры"), whenPlace.rubrics)
    }

    @Test
    fun `should parse sparse 2gis payload without schedule from resource dataset`() {
        val givenJson =
            loadImportResourceItems("import/2gis/2gis_scraped_places.json")
                .first { item -> item["id"]?.jsonPrimitive?.content == "70000001079215115" }

        val whenPlace = parser.parse(givenJson)

        assertEquals("Туалет", whenPlace.title)
        assertEquals("toilet", whenPlace.category)
        assertEquals("улица Заводская, 9", whenPlace.address)
        assertEquals("улица Заводская", whenPlace.street)
        assertEquals("9", whenPlace.houseNumber)
        assertNull(whenPlace.workingHours)
        assertEquals(emptyList(), whenPlace.attributeGroups)
        assertEquals(listOf("Туалеты"), whenPlace.rubrics)
    }
}
