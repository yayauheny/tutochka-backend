package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto

/**
 * Валидатор для создания туалета
 */
val validateRestroomOnCreate =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::address {
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
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
            minLength(1) hint "Адрес обязателен"
            maxLength(255) hint "Адрес слишком длинный (максимум 255 символов)"
        }
        RestroomUpdateDto::coordinates {
            run(validateLatLon)
        }
    }
