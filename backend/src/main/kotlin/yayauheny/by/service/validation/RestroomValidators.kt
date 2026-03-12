package yayauheny.by.service.validation

import io.konform.validation.Validation
import kotlinx.serialization.json.JsonObject
import yayauheny.by.common.errors.FieldError
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.restroom.RestroomCreateDto
import yayauheny.by.model.restroom.RestroomUpdateDto

/**
 * Валидирует nullable строковое поле на максимальную длину.
 * Используется для дополнительной валидации полей, которые не поддерживаются напрямую konform.
 */
fun validateNullableStringLength(
    fieldName: String,
    value: String?
): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    value?.let {
        if (it.length > ApiConstants.MAX_NAME_LENGTH) {
            errors.add(FieldError(fieldName, "Поле слишком длинное (максимум ${ApiConstants.MAX_NAME_LENGTH} символов)"))
        }
    }
    return errors
}

/**
 * Валидирует nullable строковое поле описания на максимальную длину.
 */
fun validateNullableDescriptionLength(
    fieldName: String,
    value: String?
): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    value?.let {
        if (it.length > ApiConstants.MAX_DESCRIPTION_LENGTH) {
            errors.add(FieldError(fieldName, "Описание слишком длинное (максимум ${ApiConstants.MAX_DESCRIPTION_LENGTH} символов)"))
        }
    }
    return errors
}

/**
 * Валидирует nullable JSON объект на максимальный размер.
 */
fun validateNullableJsonObjectSize(
    fieldName: String,
    value: JsonObject?
): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    value?.let {
        if (it.toString().length > ApiConstants.MAX_JSON_STRING_LENGTH) {
            errors.add(FieldError(fieldName, "JSON объект слишком большой (максимум ${ApiConstants.MAX_JSON_STRING_LENGTH} символов)"))
        }
    }
    return errors
}

/**
 * Валидатор для создания туалета
 */
val validateRestroomOnCreate =
    Validation<RestroomCreateDto> {
        RestroomCreateDto::coordinates {
            run(validateCoordinates)
        }
    }

/**
 * Валидатор для обновления туалета
 */
val validateRestroomOnUpdate =
    Validation<RestroomUpdateDto> {
        RestroomUpdateDto::coordinates {
            run(validateCoordinates)
        }
    }

/**
 * Валидирует все nullable поля RestroomCreateDto.
 * Используется для дополнительной валидации полей, которые не поддерживаются напрямую konform.
 */
fun validateRestroomCreateFields(dto: RestroomCreateDto): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    errors.addAll(validateNullableStringLength("name", dto.name))
    dto.address?.let {
        if (it.length > ApiConstants.MAX_ADDRESS_LENGTH) {
            errors.add(FieldError("address", "Адрес слишком длинный (максимум ${ApiConstants.MAX_ADDRESS_LENGTH} символов)"))
        }
    }
    errors.addAll(validateNullableJsonObjectSize("phones", dto.phones))
    errors.addAll(validateNullableJsonObjectSize("workTime", dto.workTime))
    errors.addAll(validateNullableJsonObjectSize("amenities", dto.amenities))
    errors.addAll(validateNullableJsonObjectSize("externalMaps", dto.externalMaps))
    errors.addAll(validateNullableStringLength("accessNote", dto.accessNote))
    errors.addAll(validateNullableStringLength("directionGuide", dto.directionGuide))
    return errors
}

/**
 * Валидирует все nullable поля RestroomUpdateDto.
 */
fun validateRestroomUpdateFields(dto: RestroomUpdateDto): List<FieldError> {
    val errors = mutableListOf<FieldError>()
    errors.addAll(validateNullableStringLength("name", dto.name))
    dto.address?.let {
        if (it.length > ApiConstants.MAX_ADDRESS_LENGTH) {
            errors.add(FieldError("address", "Адрес слишком длинный (максимум ${ApiConstants.MAX_ADDRESS_LENGTH} символов)"))
        }
    }
    errors.addAll(validateNullableJsonObjectSize("phones", dto.phones))
    errors.addAll(validateNullableJsonObjectSize("workTime", dto.workTime))
    errors.addAll(validateNullableJsonObjectSize("amenities", dto.amenities))
    errors.addAll(validateNullableJsonObjectSize("externalMaps", dto.externalMaps))
    errors.addAll(validateNullableStringLength("accessNote", dto.accessNote))
    errors.addAll(validateNullableStringLength("directionGuide", dto.directionGuide))
    return errors
}
