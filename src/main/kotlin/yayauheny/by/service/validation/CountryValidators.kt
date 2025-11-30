package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryUpdateDto

/**
 * Валидатор для создания страны
 */
val validateCountryOnCreate =
    Validation<CountryCreateDto> {
        CountryCreateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на русском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название на русском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CountryCreateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на английском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название на английском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CountryCreateDto::code {
            minLength(ApiConstants.MIN_NAME_LENGTH) hint "Код страны слишком короткий (минимум ${ApiConstants.MIN_NAME_LENGTH} символа)"
            maxLength(ApiConstants.MAX_COUNTRY_CODE_LENGTH) hint
                "Код страны слишком длинный (максимум ${ApiConstants.MAX_COUNTRY_CODE_LENGTH} символов)"
            pattern("^[A-Za-z0-9-]+$".toRegex()) hint "Код страны содержит недопустимые символы (разрешены только буквы, цифры и дефис)"
        }
    }

/**
 * Валидатор для обновления страны
 */
val validateCountryOnUpdate =
    Validation<CountryUpdateDto> {
        CountryUpdateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на русском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название на русском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CountryUpdateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на английском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название на английском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
    }
