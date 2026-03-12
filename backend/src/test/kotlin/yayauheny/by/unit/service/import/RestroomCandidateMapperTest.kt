package yayauheny.by.unit.service.import

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import yayauheny.by.model.enums.AccessibilityType
import yayauheny.by.model.enums.FeeType
import yayauheny.by.model.enums.PlaceType
import yayauheny.by.model.enums.RestroomStatus
import yayauheny.by.model.enums.LocationType
import yayauheny.by.model.enums.ImportProvider
import yayauheny.by.model.import.NormalizedRestroomCandidate
import yayauheny.by.service.import.RestroomCandidateMapper

@DisplayName("RestroomCandidateMapper Tests")
class RestroomCandidateMapperTest {
    private val cityId = UUID.randomUUID()

    @Test
    fun `should map candidate to create DTO`() {
        val candidate =
            NormalizedRestroomCandidate(
                provider = ImportProvider.TWO_GIS,
                providerObjectId = "12345",
                cityId = cityId,
                name = "Test Toilet",
                address = "Test Street 1",
                lat = 53.9,
                lng = 27.5,
                placeType = PlaceType.PUBLIC,
                locationType = LocationType.STANDALONE,
                feeType = FeeType.FREE,
                accessibilityType = AccessibilityType.WHEELCHAIR,
                status = RestroomStatus.ACTIVE,
                amenities =
                    buildJsonObject {
                        put("wifi", true)
                        put("accessible_entrance", true)
                    },
                rawSchedule = null
            )

        val dto = RestroomCandidateMapper.toCreateDto(candidate)

        assertEquals(cityId, dto.cityId)
        assertEquals("Test Toilet", dto.name)
        assertEquals("Test Street 1", dto.address)
        assertEquals(53.9, dto.coordinates.lat)
        assertEquals(27.5, dto.coordinates.lon)
        assertEquals(PlaceType.PUBLIC, dto.placeType)
        assertEquals(LocationType.STANDALONE, dto.locationType)
        assertEquals(FeeType.FREE, dto.feeType)
        assertEquals(AccessibilityType.WHEELCHAIR, dto.accessibilityType)
        assertEquals(RestroomStatus.ACTIVE, dto.status)
        assertEquals(ImportProvider.TWO_GIS, dto.originProvider)
        assertEquals("12345", dto.originId)
        assertEquals(false, dto.isHidden)
        assertNotNull(dto.externalMaps)
        assertEquals(
            "12345",
            dto.externalMaps
                ?.get("2gis")
                ?.toString()
                ?.trim('"')
        )
    }

    @Test
    fun `should build external maps for 2GIS provider`() {
        val candidate =
            NormalizedRestroomCandidate(
                provider = ImportProvider.TWO_GIS,
                providerObjectId = "12345",
                cityId = cityId,
                name = "Test",
                address = "Test Street",
                lat = 53.9,
                lng = 27.5,
                placeType = PlaceType.OTHER,
                locationType = LocationType.UNKNOWN,
                feeType = FeeType.UNKNOWN,
                accessibilityType = AccessibilityType.UNKNOWN,
                status = RestroomStatus.ACTIVE,
                amenities = buildJsonObject {},
                rawSchedule = null
            )

        val dto = RestroomCandidateMapper.toCreateDto(candidate)

        assertNotNull(dto.externalMaps)
        assertEquals(
            "12345",
            dto.externalMaps
                ?.get("2gis")
                ?.toString()
                ?.trim('"')
        )
    }
}
