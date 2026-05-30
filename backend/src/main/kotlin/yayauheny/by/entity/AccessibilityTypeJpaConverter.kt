package yayauheny.by.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import yayauheny.by.model.enums.AccessibilityType

@Converter(autoApply = false)
class AccessibilityTypeJpaConverter : AttributeConverter<AccessibilityType, String> {
    override fun convertToDatabaseColumn(attribute: AccessibilityType?): String? = attribute?.name

    override fun convertToEntityAttribute(dbData: String?): AccessibilityType =
        when (dbData?.trim()?.uppercase()) {
            null -> AccessibilityType.UNKNOWN
            "DISABLED" -> AccessibilityType.INACCESSIBLE
            else -> runCatching { AccessibilityType.valueOf(dbData) }.getOrDefault(AccessibilityType.UNKNOWN)
        }
}
