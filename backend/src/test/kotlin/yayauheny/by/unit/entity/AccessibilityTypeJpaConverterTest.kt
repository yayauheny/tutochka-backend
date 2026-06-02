package yayauheny.by.unit.entity

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.entity.AccessibilityTypeJpaConverter
import yayauheny.by.model.enums.AccessibilityType

@DisplayName("AccessibilityTypeJpaConverter Tests")
class AccessibilityTypeJpaConverterTest {
    private val converter = AccessibilityTypeJpaConverter()

    @Test
    fun `convertToEntityAttribute maps normalized enum value`() {
        val result = converter.convertToEntityAttribute(" wheelchair ")

        assertEquals(AccessibilityType.WHEELCHAIR, result)
    }

    @Test
    fun `convertToEntityAttribute maps legacy disabled value to inaccessible`() {
        val result = converter.convertToEntityAttribute("disabled")

        assertEquals(AccessibilityType.INACCESSIBLE, result)
    }

    @Test
    fun `convertToEntityAttribute returns unknown for unsupported value`() {
        val result = converter.convertToEntityAttribute("stairs-only")

        assertEquals(AccessibilityType.UNKNOWN, result)
    }
}
