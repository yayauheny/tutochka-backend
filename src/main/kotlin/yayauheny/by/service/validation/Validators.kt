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

/**
 * Валидатор для координат LatLon
 * Проверяет, что широта находится в диапазоне [-90, 90],
 * а долгота в диапазоне [-180, 180]
 */
val latLonValidator =
    Validation<LatLon> {
        LatLon::lat {
            minimum(-90.0) hint "Широта должна быть не менее -90 градусов"
            maximum(90.0) hint "Широта должна быть не более 90 градусов"
        }
        LatLon::lon {
            minimum(-180.0) hint "Долгота должна быть не менее -180 градусов"
            maximum(180.0) hint "Долгота должна быть не более 180 градусов"
        }
    }

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
            minLength(2) hint "Название города на русском языке должно содержать минимум 2 символа"
            maxLength(255) hint "Название города на русском языке слишком длинное (максимум 255 символов)"
        }
        CityCreateDto::nameEn {
            minLength(2) hint "Название города на английском языке должно содержать минимум 2 символа"
            maxLength(255) hint "Название города на английском языке слишком длинное (максимум 255 символов)"
        }
        // region - nullable поле, валидация выполняется на уровне сервиса
        // для region: если не null, то длина 2..255
        CityCreateDto::coordinates {
            run(latLonValidator)
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
        CityUpdateDto::coordinates {
            run(latLonValidator)
        }
    }

val restroomCreateValidator =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        RestroomCreateDto::coordinates {
            run(latLonValidator)
        }
    }

val restroomUpdateValidator =
    Validation<RestroomUpdateDto> {
        RestroomUpdateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        RestroomUpdateDto::coordinates {
            run(latLonValidator)
        }
    }

data class NearestRestroomsParams(
    val coordinates: LatLon,
    val limit: Int,
    val distanceMeters: Int
)

val nearestRestroomsParamsValidator =
    Validation<NearestRestroomsParams> {
        NearestRestroomsParams::coordinates {
            run(latLonValidator)
        }
        NearestRestroomsParams::limit {
            minimum(1) hint "Лимит должен быть не менее 1"
            maximum(10) hint "Лимит должен быть не более 100"
        }
        NearestRestroomsParams::distanceMeters {
            minimum(1) hint "Радиус поиска должен быть не менее 1 метра"
            maximum(50000) hint "Радиус поиска должен быть не более 50000 метров (50 км)"
        }
    }
