package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import io.konform.validation.constraints.pattern
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityUpdateDto
import yayauheny.by.model.country.CountryCreateDto
import yayauheny.by.model.country.CountryUpdateDto
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto

val countryCreateValidator =
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

val countryUpdateValidator =
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

val cityCreateValidator =
    Validation<CityCreateDto> {
        CityCreateDto::nameRu {
            minLength(1) hint "Название города на русском языке обязательно"
            maxLength(255) hint "Название города на русском языке слишком длинное (максимум 255 символов)"
        }
        CityCreateDto::nameEn {
            minLength(1) hint "Название города на английском языке обязательно"
            maxLength(255) hint "Название города на английском языке слишком длинное (максимум 255 символов)"
        }
        CityCreateDto::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        CityCreateDto::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

val cityUpdateValidator =
    Validation<CityUpdateDto> {
        CityUpdateDto::nameRu {
            minLength(1) hint "Название города на русском языке обязательно"
            maxLength(255) hint "Название города на русском языке слишком длинное (максимум 255 символов)"
        }
        CityUpdateDto::nameEn {
            minLength(1) hint "Название города на английском языке обязательно"
            maxLength(255) hint "Название города на английском языке слишком длинное (максимум 255 символов)"
        }
        CityUpdateDto::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        CityUpdateDto::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

val restroomCreateValidator =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        RestroomCreateDto::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        RestroomCreateDto::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

val restroomUpdateValidator =
    Validation<RestroomUpdateDto> {
        RestroomUpdateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        RestroomUpdateDto::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        RestroomUpdateDto::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

data class NearestRestroomsParams(
    val lat: Double,
    val lon: Double,
    val limit: Int
)

val nearestRestroomsParamsValidator =
    Validation<NearestRestroomsParams> {
        NearestRestroomsParams::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        NearestRestroomsParams::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
        NearestRestroomsParams::limit {
            minimum(1) hint "Лимит должен быть не менее 1"
            maximum(100) hint "Лимит должен быть не более 100"
        }
    }
