package yayauheny.by.unit.importing.provider.yandex

import kotlin.test.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.model.ImportAdapterContext
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedParser
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedLocation
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedPlace
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportPayloadType

@DisplayName("YandexMapsScrapedPlace toCommonModel Tests")
class YandexMapsScrapedNormalizerTest {
    private val parser = YandexMapsScrapedParser()

    @Test
    fun `should detect free toilet before paid keyword`() {
        val place =
            YandexMapsScrapedPlace(
                title = "Бесплатный туалет",
                location = YandexMapsScrapedLocation(lat = 53.9, lng = 27.56),
                placeId = "yandex-1",
                features = listOf("бесплатный туалет")
            )

        val candidate =
            parser.toCommonModel(
                place,
                ImportAdapterContext(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON, java.util.UUID.randomUUID())
            )

        assertEquals(FeeType.FREE, candidate.feeType)
    }

    @Test
    fun `should detect paid toilet when free keyword is absent`() {
        val place =
            YandexMapsScrapedPlace(
                title = "Платный туалет",
                location = YandexMapsScrapedLocation(lat = 53.9, lng = 27.56),
                placeId = "yandex-2",
                features = listOf("платный туалет")
            )

        val candidate =
            parser.toCommonModel(
                place,
                ImportAdapterContext(ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON, java.util.UUID.randomUUID())
            )

        assertEquals(FeeType.PAID, candidate.feeType)
    }
}
