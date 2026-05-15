package yayauheny.by.unit.importing.provider.yandex

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedLocation
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedPlace
import yayauheny.by.importing.provider.yandex.YandexMapsScrapedWorkingHour
import yayauheny.by.importing.provider.yandex.YandexNormalizationProfile
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.RestroomStatus

@DisplayName("YandexNormalizationProfile Tests")
class YandexNormalizationProfileTest {
    @Test
    fun `short title wins over title`() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Long Name",
                    shortTitle = "Short Name",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertEquals("Short Name", resolved.name)
    }

    @Test
    fun `title fallback is preserved when short title is missing`() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Main Name",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertEquals("Main Name", resolved.name)
    }

    @Test
    fun `missing names fall back to default toilet name`() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = null,
                    shortTitle = null,
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertEquals("Туалет", resolved.name)
    }

    @Test
    fun `address cleanup and location heuristic are preserved`() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    address = " Торговый центр Green ",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1"
                )
            )

        assertEquals("Торговый центр Green", resolved.address)
        assertEquals(LocationType.INSIDE_BUILDING, resolved.locationType)
    }

    @Test
    fun `fee status amenities and raw schedule are preserved`() {
        val workingHours =
            listOf(
                YandexMapsScrapedWorkingHour(
                    day = "mon",
                    from = "10:00",
                    to = "18:00"
                )
            )
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Бесплатный туалет",
                    address = "Mall",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    statusText = "закрыт",
                    features = listOf("доступно", "туалет для людей с инвалидностью"),
                    workingHours = workingHours
                )
            )

        assertEquals(FeeType.FREE, resolved.feeType)
        assertEquals(RestroomStatus.INACTIVE, resolved.status)
        assertNotNull(resolved.amenities["accessible_entrance"])
        assertNotNull(resolved.amenities["accessible_toilet"])
        assertNotNull(resolved.rawSchedule)
    }

    @Test
    fun `building context is preserved for inside building locations`() {
        val rawSchedule =
            buildJsonObject {
                put(
                    "mon",
                    buildJsonObject {
                        put("from", JsonPrimitive("10:00"))
                        put("to", JsonPrimitive("18:00"))
                    }
                )
            }
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    address = "Торговый центр Green",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    workingHours = listOf(YandexMapsScrapedWorkingHour(day = "mon", from = "10:00", to = "18:00"))
                )
            )

        assertNotNull(resolved.buildingContext)
        assertEquals("Торговый центр Green", resolved.buildingContext!!.address)
        assertEquals(rawSchedule, resolved.rawSchedule)
    }

    @Test
    fun `raw schedule preserves structured intervals`() {
        val resolved =
            YandexNormalizationProfile.resolve(
                YandexMapsScrapedPlace(
                    title = "Туалет",
                    location = YandexMapsScrapedLocation(53.9, 27.56),
                    placeId = "yandex-1",
                    workingHours =
                        listOf(
                            YandexMapsScrapedWorkingHour(
                                day = "mon",
                                from = "09:00",
                                to = "12:00",
                                intervals =
                                    listOf(
                                        yayauheny.by.importing.provider.yandex.YandexMapsScrapedWorkingInterval(
                                            from = "09:00",
                                            to = "12:00"
                                        ),
                                        yayauheny.by.importing.provider.yandex.YandexMapsScrapedWorkingInterval(
                                            from = "12:30",
                                            to = "21:00"
                                        )
                                    )
                            )
                        )
                )
            )

        assertEquals(
            """{"mon":{"from":"09:00","to":"12:00","intervals":[{"from":"09:00","to":"12:00"},{"from":"12:30","to":"21:00"}]}}""",
            resolved.rawSchedule.toString()
        )
    }
}
