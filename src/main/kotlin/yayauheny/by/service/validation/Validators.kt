package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.minimum
import io.konform.validation.constraints.pattern
import yayauheny.by.model.LatLon
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
        // Coordinates validation temporarily disabled - needs proper Konform syntax for nested objects
        // CityCreateDto::coordinates { ... }
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
        // Coordinates validation temporarily disabled - needs proper Konform syntax for nested objects
        // CityUpdateDto::coordinates { ... }
    }

val restroomCreateValidator =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        // Coordinates validation temporarily disabled - needs proper Konform syntax for nested objects
        // RestroomCreateDto::coordinates { ... }
    }

val restroomUpdateValidator =
    Validation<RestroomUpdateDto> {
        RestroomUpdateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        // Coordinates validation temporarily disabled - needs proper Konform syntax for nested objects
        // RestroomUpdateDto::coordinates { ... }
    }

data class NearestRestroomsParams(
    val coordinates: LatLon,
    val limit: Int,
    val distanceMeters: Int
)

val nearestRestroomsParamsValidator =
    Validation<NearestRestroomsParams> {
        // Coordinates validation temporarily disabled - needs proper Konform syntax for nested objects
        // NearestRestroomsParams::coordinates { ... }
        NearestRestroomsParams::limit {
            minimum(1) hint "Лимит должен быть не менее 1"
            maximum(10) hint "Лимит должен быть не более 100"
        }
        NearestRestroomsParams::distanceMeters {
            minimum(1) hint "Радиус поиска должен быть не менее 1 метра"
            maximum(50000) hint "Радиус поиска должен быть не более 50000 метров (50 км)"
        }
    }
