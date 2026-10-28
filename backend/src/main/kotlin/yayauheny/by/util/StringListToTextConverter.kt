package yayauheny.by.util

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter
class StringListToTextConverter : AttributeConverter<List<String>?, String?> {
    override fun convertToDatabaseColumn(attribute: List<String>?): String? {
        val values = attribute?.filter { it.isNotBlank() } ?: return null
        return values.takeIf { it.isNotEmpty() }?.joinToString("|")
    }

    override fun convertToEntityAttribute(dbData: String?): List<String>? {
        val values = dbData?.split("|")?.map { it.trim() }?.filter { it.isNotBlank() } ?: return null
        return values.takeIf { it.isNotEmpty() }
    }
}
