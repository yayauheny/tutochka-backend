package yayauheny.by.unit.importing.provider.twogis

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.provider.twogis.TwoGisNormalizationProfile
import yayauheny.by.importing.provider.twogis.TwoGisScrapedLocation
import yayauheny.by.importing.provider.twogis.TwoGisScrapedPlace
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.GenderType
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus

@DisplayName("TwoGisNormalizationProfile Tests")
class TwoGisNormalizationProfileTest {
    @Test
    fun `missing title resolves to default toilet name`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = null,
                    address = "Street 1",
                    location = TwoGisScrapedLocation(53.9, 27.5)
                )
            )

        assertEquals("Туалет", resolved.name)
    }

    @Test
    fun `raw address wins when present`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    address = "Street 1",
                    street = "Other Street",
                    houseNumber = "2",
                    location = TwoGisScrapedLocation(53.9, 27.5)
                )
            )

        assertEquals("Street 1", resolved.address)
    }

    @Test
    fun `missing address resolves from street and house number`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    street = "Рудобельская",
                    houseNumber = "3",
                    location = TwoGisScrapedLocation(53.9, 27.5)
                )
            )

        assertEquals("Рудобельская, 3", resolved.address)
    }

    @Test
    fun `missing address street and house number resolves to null address`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = null,
                    location = TwoGisScrapedLocation(53.9, 27.5)
                )
            )

        assertNull(resolved.address)
    }

    @Test
    fun `category and rubrics preserve location and place type resolution`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Cafe Toilet",
                    category = null,
                    rubrics = listOf("Кафе"),
                    location = TwoGisScrapedLocation(53.9, 27.5)
                )
            )

        assertEquals(LocationType.INSIDE_BUILDING, resolved.locationType)
        assertEquals(PlaceType.RESTAURANT, resolved.placeType)
    }

    @Test
    fun `attributes preserve fee status and amenities behavior`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    category = "toilet",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    attributeGroups = listOf("платный туалет", "доступ ограничен", "оплата картой", "пандус")
                )
            )

        assertEquals(FeeType.PAID, resolved.feeType)
        assertEquals(RestroomStatus.INACTIVE, resolved.status)
        assertNotNull(resolved.amenities["payment_methods"])
        assertNotNull(resolved.amenities["ramp"])
    }

    @Test
    fun `inside building preserves building context and gender resolution`() {
        val workingHours = buildJsonObject { put("mon", "10:00-18:00") }
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Женский туалет",
                    category = "mall",
                    address = null,
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    workingHours = workingHours
                )
            )

        assertNotNull(resolved.buildingContext)
        assertEquals("Женский туалет", resolved.buildingContext!!.name)
        assertEquals("Женский туалет", resolved.buildingContext!!.address)
        assertEquals(workingHours, resolved.rawSchedule)
        assertEquals(workingHours, resolved.buildingContext!!.workTime)
        assertEquals(GenderType.WOMEN, resolved.genderType)
    }
}
