package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryUpdateDto

/**
 * Валидатор для создания страны
 */
val validateCountryOnCreate =
    Validation<CountryCreateDto> {
        CountryCreateDto::nameRu {
            minLength(1) hint "Название на русском языке обязательно"
            maxLength(255) hint "Название на русском языке слишком длинное (максимум 255 символов)"
        }
        CountryCreateDto::nameEn {
            minLength(1) hint "Название на английском языке обязательно"
            maxLength(255) hint "Название на английском языке слишком длинное (максимум 255 символов)"
        }
        CountryCreateDto::code {
            minLength(2) hint "Код страны слишком короткий (минимум 2 символа)"
            maxLength(10) hint "Код страны слишком длинный (максимум 10 символов)"
            pattern("^[A-Za-z0-9-]+$".toRegex()) hint "Код страны содержит недопустимые символы (разрешены только буквы, цифры и дефис)"
        }
    }

/**
 * Валидатор для обновления страны
 */
val validateCountryOnUpdate =
    Validation<CountryUpdateDto> {
        CountryUpdateDto::nameRu {
            minLength(1) hint "Название на русском языке обязательно"
            maxLength(255) hint "Название на русском языке слишком длинное (максимум 255 символов)"
        }
        CountryUpdateDto::nameEn {
            minLength(1) hint "Название на английском языке обязательно"
            maxLength(255) hint "Название на английском языке слишком длинное (максимум 255 символов)"
        }
    }
