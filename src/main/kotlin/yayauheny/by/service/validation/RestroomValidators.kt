package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto

/**
 * Валидатор для создания туалета
 */
val validateRestroomOnCreate =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::address {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Адрес обязателен"
            maxLength(ApiConstants.MAX_ADDRESS_LENGTH) hint "Адрес слишком длинный (максимум ${ApiConstants.MAX_ADDRESS_LENGTH} символов)"
        }
        RestroomCreateDto::coordinates {
            run(validateLatLon)
        }
    }

/**
 * Валидатор для обновления туалета
 */
val validateRestroomOnUpdate =
    Validation<RestroomUpdateDto> {
        RestroomUpdateDto::address {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Адрес обязателен"
            maxLength(ApiConstants.MAX_ADDRESS_LENGTH) hint "Адрес слишком длинный (максимум ${ApiConstants.MAX_ADDRESS_LENGTH} символов)"
        }
        RestroomUpdateDto::coordinates {
            run(validateLatLon)
        }
    }
