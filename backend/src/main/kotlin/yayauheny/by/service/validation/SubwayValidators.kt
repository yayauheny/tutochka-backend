package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import io.konform.validation.constraints.pattern
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.subway.SubwayLineCreateDto
import yayauheny.by.model.subway.SubwayStationCreateDto

val validateSubwayLineOnCreate =
    Validation<SubwayLineCreateDto> {
        SubwayLineCreateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на русском языке обязательно"
            maxLength(100) hint "Название на русском языке слишком длинное (максимум 100 символов)"
        }
        SubwayLineCreateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на английском языке обязательно"
            maxLength(100) hint "Название на английском языке слишком длинное (максимум 100 символов)"
        }
        SubwayLineCreateDto::hexColor {
            minLength(7) hint "Цвет должен быть в формате #RRGGBB (7 символов)"
            maxLength(7) hint "Цвет должен быть в формате #RRGGBB (7 символов)"
            pattern("^#[0-9A-Fa-f]{6}$".toRegex()) hint "Цвет должен быть в формате HEX (#RRGGBB), например #FF0000"
        }
    }

val validateSubwayStationOnCreate =
    Validation<SubwayStationCreateDto> {
        SubwayStationCreateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на русском языке обязательно"
            maxLength(255) hint "Название на русском языке слишком длинное (максимум 255 символов)"
        }
        SubwayStationCreateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название на английском языке обязательно"
            maxLength(255) hint "Название на английском языке слишком длинное (максимум 255 символов)"
        }
        SubwayStationCreateDto::coordinates {
            run(validateCoordinates)
        }
    }
