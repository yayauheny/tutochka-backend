package yayauheny.by.unit.service.import.twogis

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.import.twogis.TwoGisScrapedLocation
import yayauheny.by.model.import.twogis.TwoGisScrapedPlace
import yayauheny.by.service.import.twogis.TwoGisScrapedNormalizer

@DisplayName("TwoGisScrapedNormalizer Tests")
class TwoGisScrapedNormalizerTest {
    private val normalizer = TwoGisScrapedNormalizer()
    private val cityId = UUID.randomUUID()

    @Test
    fun `should normalize standalone toilet`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Public Toilet",
                category = "toilet",
                address = "Test Street 1",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(LocationType.STANDALONE, candidate.locationType)
        assertEquals(PlaceType.PUBLIC, candidate.placeType)
        assertEquals(cityId, candidate.cityId)
        assertEquals("12345", candidate.providerObjectId)
        assertEquals("Public Toilet", candidate.name)
        assertEquals("Test Street 1", candidate.address)
        assertEquals(53.9, candidate.lat)
        assertEquals(27.5, candidate.lng)
    }

    @Test
    fun `should normalize place inside venue and set buildingContext`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Mall Toilet",
                category = "mall",
                address = "Mall Street 1",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(LocationType.INSIDE_BUILDING, candidate.locationType)
        assertEquals(PlaceType.MALL, candidate.placeType)
        assertNotNull(candidate.buildingContext)
        assertEquals("Mall Toilet", candidate.buildingContext!!.name)
        assertEquals("Mall Street 1", candidate.buildingContext!!.address)
        assertEquals("12345", candidate.buildingContext!!.externalId)
    }

    @Test
    fun `should resolve location from rubrics when category is empty`() {
        val place =
            TwoGisScrapedPlace(
                id = "id1",
                title = "Cafe Toilet",
                category = null,
                address = "Cafe St 1",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = listOf("Кафе")
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(LocationType.INSIDE_BUILDING, candidate.locationType)
        assertEquals(PlaceType.RESTAURANT, candidate.placeType)
        assertNotNull(candidate.buildingContext)
        assertEquals("id1", candidate.buildingContext!!.externalId)
    }

    @Test
    fun `should resolve standalone from rubrics when category is empty`() {
        val place =
            TwoGisScrapedPlace(
                id = "id2",
                title = "Public Toilet",
                category = null,
                address = "Street 2",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = listOf("Туалеты")
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(LocationType.STANDALONE, candidate.locationType)
        assertEquals(PlaceType.PUBLIC, candidate.placeType)
        assertNull(candidate.buildingContext)
    }

    @Test
    fun `should normalize unknown category`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Unknown Place",
                category = null,
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(LocationType.UNKNOWN, candidate.locationType)
        assertEquals(PlaceType.OTHER, candidate.placeType)
    }

    @Test
    fun `should detect paid toilet from attributes`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Paid Toilet",
                category = "toilet",
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = listOf("платный туалет", "оплата картой"),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(FeeType.PAID, candidate.feeType)
    }

    @Test
    fun `should detect free toilet from attributes`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Free Toilet",
                category = "toilet",
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = listOf("бесплатный туалет"),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(FeeType.FREE, candidate.feeType)
    }

    @Test
    fun `should default to unknown fee type`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Toilet",
                category = "toilet",
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = emptyList(),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(FeeType.UNKNOWN, candidate.feeType)
    }

    @Test
    fun `should set status to inactive for access limited`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Restricted Toilet",
                category = "toilet",
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups = listOf("доступ ограничен"),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertEquals(RestroomStatus.INACTIVE, candidate.status)
    }

    @Test
    fun `should build amenities from attributes`() {
        val place =
            TwoGisScrapedPlace(
                id = "12345",
                title = "Toilet",
                category = "toilet",
                address = "Test Street",
                location = TwoGisScrapedLocation(lat = 53.9, lng = 27.5),
                workingHours = null,
                attributeGroups =
                    listOf(
                        "оплата картой",
                        "пандус",
                        "туалет для маломобильных людей"
                    ),
                rubrics = emptyList()
            )

        val candidate = normalizer.normalize(cityId, place, ImportPayloadType.TWO_GIS_SCRAPED_PLACE_JSON)

        assertNotNull(candidate.amenities)
        val amenities = candidate.amenities
        assertNotNull(amenities["payment_methods"])
        // Проверяем наличие полей (значения могут быть JsonPrimitive или JsonArray)
        assertNotNull(amenities["accessible_entrance"])
        assertNotNull(amenities["accessible_toilet"])
        assertNotNull(amenities["ramp"])
    }
}
