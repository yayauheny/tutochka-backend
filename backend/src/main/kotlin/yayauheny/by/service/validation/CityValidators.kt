package yayauheny.by.service.validation

import io.konform.validation.Validation
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minLength
import yayauheny.by.common.errors.FieldError
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.city.CityCreateDto
import yayauheny.by.model.city.CityUpdateDto

/**
 * Параметры для поиска городов
 */
data class CitySearchParams(
    val name: String
)

/**
 * Валидатор для создания города
 */
val validateCityOnCreate =
    Validation<CityCreateDto> {
        CityCreateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH) hint
                "Название города на русском языке должно содержать минимум ${ApiConstants.MIN_NAME_LENGTH} символа"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название города на русском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CityCreateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH) hint
                "Название города на английском языке должно содержать минимум ${ApiConstants.MIN_NAME_LENGTH} символа"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название города на английском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CityCreateDto::coordinates {
            run(validateCoordinates)
        }
    }

/**
 * Валидатор для обновления города
 */
val validateCityOnUpdate =
    Validation<CityUpdateDto> {
        CityUpdateDto::nameRu {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название города на русском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название города на русском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CityUpdateDto::nameEn {
            minLength(ApiConstants.MIN_NAME_LENGTH_REQUIRED) hint "Название города на английском языке обязательно"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint
                "Название города на английском языке слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
        CityUpdateDto::coordinates {
            run(validateCoordinates)
        }
    }

/**
 * Валидатор для параметров поиска городов
 */
val validateCitySearchParams =
    Validation<CitySearchParams> {
        CitySearchParams::name {
            minLength(ApiConstants.MIN_NAME_LENGTH) hint "Минимальная длина параметра name — ${ApiConstants.MIN_NAME_LENGTH} символа"
            maxLength(ApiConstants.MAX_NAME_LENGTH) hint "Параметр name слишком длинный (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"
        }
    }

/**
 * Валидирует nullable строковое поле region.
 * Используется для дополнительной валидации полей, которые не поддерживаются напрямую konform.
 *
 * @param region значение региона для валидации
 * @return список ошибок валидации (пустой, если валидация прошла успешно)
 */
fun validateRegion(region: String?): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    region?.let {
        when {
            it.length < ApiConstants.MIN_NAME_LENGTH ->
                errors.add(
                    FieldError("region", "Регион должен содержать минимум ${ApiConstants.MIN_NAME_LENGTH} символа")
                )
            it.length > ApiConstants.MAX_REGION_LENGTH ->
                errors.add(
                    FieldError("region", "Регион слишком длинный (максимум ${ApiConstants.MAX_REGION_LENGTH} символов)")
                )
        }
    }
    return errors
}
