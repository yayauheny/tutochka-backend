package yayauheny.by.entity

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import yayauheny.by.model.enums.PlaceType

@Converter(autoApply = false)
class PlaceTypeJpaConverter : AttributeConverter<PlaceType, String> {
    override fun convertToDatabaseColumn(attribute: PlaceType?): String? = attribute?.code

    override fun convertToEntityAttribute(dbData: String?): PlaceType = PlaceType.fromCode(dbData)
}
