package yayauheny.by.unit.importing.provider.yandex

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.loadImportResourceItems
import yayauheny.by.importing.exception.InvalidImportPayload
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedParser
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.toJacksonNode

@DisplayName("YandexMapsScrapedParser Tests")
class YandexMapsScrapedParserTest {
    private val parser = YandexMapsScrapedParser()

    @Test
    fun should_parse_minimal_yandex_place() {
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

        val whenPlace = parser.parse(givenJson.toJacksonNode())

        assertThat(whenPlace.title).isEqualTo("Туалет")
        assertThat(whenPlace.placeId).isEqualTo("yandex-123")
        assertThat(whenPlace.location.lat).isEqualTo(53.9)
        assertThat(whenPlace.location.lng).isEqualTo(27.56)
    }

    @Test
    fun should_parse_rich_yandex_payload_from_resource_dataset() {
        val givenJson =
            loadImportResourceItems("import/yandex/yandex_scraped_places.json")
                .first { item -> item["placeId"]?.jsonPrimitive?.content == "137246000290" }

        val whenPlace = parser.parse(givenJson.toJacksonNode())
        val monday =
            whenPlace.workingHours!!
                .first { workingHour -> workingHour.day == "Mon" }

        assertThat(whenPlace.shortTitle).isEqualTo("Туалет")
        assertThat(whenPlace.address).isEqualTo("Минск, ул. Киселёва, 3")
        assertThat(whenPlace.statusText).isEqualTo("Закрыто до завтра")
        assertThat(whenPlace.isOpenNow).isFalse()
        assertThat(monday.day).isEqualTo("Mon")
        assertThat(monday.intervals).hasSize(2)
        assertThat(whenPlace.features).containsExactly("недоступно")
    }

    @Test
    fun should_parse_accessible_yandex_payload_without_working_hours_from_resource_dataset() {
        val givenJson =
            loadImportResourceItems("import/yandex/yandex_scraped_places.json")
                .first { item -> item["placeId"]?.jsonPrimitive?.content == "88183159086" }

        val whenPlace = parser.parse(givenJson.toJacksonNode())

        assertThat(whenPlace.title).isEqualTo("Туалет")
        assertThat(whenPlace.address).isEqualTo("д. Боровляны, Малиновская ул., 2Б")
        assertThat(whenPlace.workingHours).isNull()
        assertThat(whenPlace.features).containsExactly(
            "туалет для людей с инвалидностью",
            "пандус",
            "доступно",
            "парковка для людей с инвалидностью",
            "лифт"
        )
    }

    @Test
    fun should_fail_when_place_id_is_missing() {
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

        org.assertj.core.api.Assertions.assertThatThrownBy {
            parser.parse(givenJson.toJacksonNode())
        }.isInstanceOf(InvalidImportPayload::class.java)
    }
}
