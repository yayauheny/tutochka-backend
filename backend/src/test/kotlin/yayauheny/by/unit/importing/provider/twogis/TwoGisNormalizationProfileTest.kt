package yayauheny.by.unit.importing.provider.twogis

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.toJacksonNode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import yayauheny.by.importing.provider.twogis.TwoGisNormalizationProfile
import yayauheny.by.importing.provider.twogis.TwoGisScrapedLocation
import yayauheny.by.importing.provider.twogis.TwoGisScrapedPlace
import yayauheny.by.model.enums.AccessibilityType
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

    @ParameterizedTest
    @CsvSource(
        "Бесплатный туалет, FREE",
        "Платный туалет, PAID"
    )
    fun `historical fee attributes still map to current fee type`(
        attribute: String,
        expectedFeeType: FeeType
    ) {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    category = "toilet",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    attributeGroups = listOf(attribute)
                )
            )

        assertEquals(expectedFeeType, resolved.feeType)
    }

    @Test
    fun `historical payment attributes still map to payment methods`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    category = "toilet",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    attributeGroups = listOf(
                        "Оплата картой",
                        "Наличный расчёт",
                        "Оплата по QR-коду",
                        "Оплата через банк",
                        "Оплата через приложение",
                        "Перевод с карты"
                    )
                )
            )

        assertEquals(
            setOf("card", "cash", "qr", "bank", "app", "card_transfer"),
            resolved.amenities["payment_methods"].map { it.asText() }.toSet()
        )
    }

    @Test
    fun `historical accessibility attributes still map to current accessibility fields`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    category = "toilet",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    attributeGroups = listOf(
                        "Туалет для маломобильных людей",
                        "Доступный вход для людей с инвалидностью",
                        "Пандус",
                        "Подъёмник",
                        "Широкий лифт",
                        "Нет двери / есть автоматическая дверь",
                        "Доступно",
                        "Доступ ограничен"
                    )
                )
            )

        assertEquals(AccessibilityType.WHEELCHAIR, resolved.accessibilityType)
        assertEquals(RestroomStatus.INACTIVE, resolved.status)
        assertTrue(resolved.amenities["accessible_entrance"].asBoolean())
        assertTrue(resolved.amenities["accessible_toilet"].asBoolean())
        assertTrue(resolved.amenities["ramp"].asBoolean())
        assertTrue(resolved.amenities["lift"].asBoolean())
        assertTrue(resolved.amenities["automatic_door"].asBoolean())
        assertTrue(resolved.amenities["access_limited"].asBoolean())
    }

    @Test
    fun `plain toilet attribute stays a no-op marker`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    category = "toilet",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    attributeGroups = listOf("Туалет")
                )
            )

        assertEquals(FeeType.UNKNOWN, resolved.feeType)
        assertEquals(AccessibilityType.UNKNOWN, resolved.accessibilityType)
        assertEquals(RestroomStatus.ACTIVE, resolved.status)
        assertFalse(resolved.amenities["accessible_entrance"].asBoolean())
        assertFalse(resolved.amenities["accessible_toilet"].asBoolean())
    }

    @Test
    fun `inside building preserves building context and gender resolution`() {
        val workingHours = buildJsonObject { put("mon", "10:00-18:00") }.toJacksonNode()
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

    @Test
    fun `blank address and empty working hours are normalized consistently`() {
        val resolved =
            TwoGisNormalizationProfile.resolve(
                TwoGisScrapedPlace(
                    id = "1",
                    title = "Toilet",
                    address = " null ",
                    street = " ",
                    houseNumber = " ",
                    location = TwoGisScrapedLocation(53.9, 27.5),
                    workingHours = buildJsonObject {}.toJacksonNode()
                )
            )

        assertNull(resolved.address)
        assertNull(resolved.rawSchedule)
    }
}
