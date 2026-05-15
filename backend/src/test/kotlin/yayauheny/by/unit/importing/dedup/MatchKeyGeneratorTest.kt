package yayauheny.by.unit.importing.dedup

import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.importing.dedup.MatchKeyGenerator
import yayauheny.by.model.enums.LocationType

@DisplayName("MatchKeyGenerator Tests")
class MatchKeyGeneratorTest {
    @Test
    fun `normalization is deterministic`() {
        assertEquals(
            "улица ленина 10",
            MatchKeyGenerator.normalizeText("  Улица   Ленина, 10  ")
        )
        assertEquals(
            "green time",
            MatchKeyGenerator.normalizeText(" green time ")
        )
    }

    @Test
    fun `missing required fields suppress match key generation`() {
        val cityId = UUID.randomUUID()

        assertNull(
            MatchKeyGenerator.buildingMatchKey(
                cityId = cityId,
                address = null,
                lat = 53.9,
                lon = 27.5
            )
        )
        assertNull(
            MatchKeyGenerator.restroomMatchKey(
                cityId = cityId,
                buildingId = null,
                address = null,
                name = "Toilet",
                lat = 53.9,
                lon = 27.5,
                locationType = LocationType.STANDALONE
            )
        )
        assertNull(
            MatchKeyGenerator.restroomMatchKey(
                cityId = cityId,
                buildingId = null,
                address = "Street 1",
                name = "Toilet",
                lat = 53.9,
                lon = 27.5,
                locationType = LocationType.INSIDE_BUILDING
            )
        )
    }
}
