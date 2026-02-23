package yayauheny.by.service.import.twogis

import yayauheny.by.model.enums.GenderType

private const val MALE_MARKER = "мужской"
private const val FEMALE_MARKER = "женский"

/**
 * Определяет тип туалета по половому признаку из заголовка места 2ГИС.
 * Источник — поле title (например «Мужской туалет», «Женский платный туалет», «Женский туалет, Мужской платный туалет»).
 */
object TwoGisGenderFromTitleResolver {
    fun resolve(title: String?): GenderType {
        if (title.isNullOrBlank()) return GenderType.UNKNOWN
        val lower = title.trim().lowercase()
        val hasMale = lower.contains(MALE_MARKER)
        val hasFemale = lower.contains(FEMALE_MARKER)
        return when {
            hasMale && hasFemale -> GenderType.UNISEX
            hasMale -> GenderType.MEN
            hasFemale -> GenderType.WOMEN
            else -> GenderType.UNKNOWN
        }
    }
}
