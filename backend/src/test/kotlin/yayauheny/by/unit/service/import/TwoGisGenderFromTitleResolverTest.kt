package yayauheny.by.unit.service.import

import kotlin.test.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.model.enums.GenderType
import yayauheny.by.service.import.twogis.TwoGisGenderFromTitleResolver

@DisplayName("TwoGisGenderFromTitleResolver")
class TwoGisGenderFromTitleResolverTest {
    @Test
    @DisplayName("returns MEN when title contains only мужской")
    fun menWhenMaleOnly() {
        assertEquals(GenderType.MEN, TwoGisGenderFromTitleResolver.resolve("Мужской туалет"))
        assertEquals(GenderType.MEN, TwoGisGenderFromTitleResolver.resolve("Мужской платный туалет"))
        assertEquals(GenderType.MEN, TwoGisGenderFromTitleResolver.resolve("МУЖСКОЙ ТУАЛЕТ"))
    }

    @Test
    @DisplayName("returns WOMEN when title contains only женский")
    fun womenWhenFemaleOnly() {
        assertEquals(GenderType.WOMEN, TwoGisGenderFromTitleResolver.resolve("Женский туалет"))
        assertEquals(GenderType.WOMEN, TwoGisGenderFromTitleResolver.resolve("Женский платный туалет"))
        assertEquals(GenderType.WOMEN, TwoGisGenderFromTitleResolver.resolve("женский туалет"))
    }

    @Test
    @DisplayName("returns UNISEX when title contains both мужской and женский")
    fun unisexWhenBoth() {
        assertEquals(GenderType.UNISEX, TwoGisGenderFromTitleResolver.resolve("Женский туалет, Мужской платный туалет"))
        assertEquals(GenderType.UNISEX, TwoGisGenderFromTitleResolver.resolve("Мужской туалет, Женский туалет"))
    }

    @Test
    @DisplayName("returns UNKNOWN when title has no gender markers or is blank")
    fun unknownWhenNoMarkersOrBlank() {
        assertEquals(GenderType.UNKNOWN, TwoGisGenderFromTitleResolver.resolve("GreenTime, торгово-развлекательный центр"))
        assertEquals(GenderType.UNKNOWN, TwoGisGenderFromTitleResolver.resolve("Туалет"))
        assertEquals(GenderType.UNKNOWN, TwoGisGenderFromTitleResolver.resolve(""))
        assertEquals(GenderType.UNKNOWN, TwoGisGenderFromTitleResolver.resolve("   "))
        assertEquals(GenderType.UNKNOWN, TwoGisGenderFromTitleResolver.resolve(null))
    }
}
