package yayauheny.by.unit.service.import.yandex

import kotlin.test.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.import.yandex.YandexMapsScrapedLocation
import yayauheny.by.model.import.yandex.YandexMapsScrapedPlace
import yayauheny.by.service.import.yandex.YandexMapsScrapedNormalizer

@DisplayName("YandexMapsScrapedNormalizer Tests")
class YandexMapsScrapedNormalizerTest {
    private val normalizer = YandexMapsScrapedNormalizer()

    @Test
    fun `should detect free toilet before paid keyword`() {
        val place =
            YandexMapsScrapedPlace(
                title = "Бесплатный туалет",
                location = YandexMapsScrapedLocation(lat = 53.9, lng = 27.56),
                placeId = "yandex-1",
                features = listOf("бесплатный туалет")
            )

        val candidate = normalizer.normalize(java.util.UUID.randomUUID(), place, ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON)

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

        val candidate = normalizer.normalize(java.util.UUID.randomUUID(), place, ImportPayloadType.YANDEX_MAPS_SCRAPED_PLACE_JSON)

        assertEquals(FeeType.PAID, candidate.feeType)
    }
}
