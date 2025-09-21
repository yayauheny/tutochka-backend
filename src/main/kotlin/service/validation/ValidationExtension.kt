package service.validation

import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import io.konform.validation.ValidationError
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException

sealed class Validated<T> {
    data class Ok<T>(
        val value: T
    ) : Validated<T>()

    data class Fail<T>(
        val value: T,
        val errors: List<FieldError>
    ) : Validated<T>()
}

fun ValidationError.pathAsString(): String {
    // Попробуем использовать более стабильный подход
    return try {
        // Проверяем, есть ли dataPath (более новая версия Konform)
        val dataPath = this::class.java.getDeclaredField("dataPath")
        dataPath.isAccessible = true
        val pathValue = dataPath.get(this) as? List<*>
        if (pathValue != null && pathValue.isNotEmpty()) {
            pathValue.joinToString(".") { it.toString() }
        } else {
            // Fallback к старому методу, но с улучшенным парсингом
            val pathStr = this.path.toString()
            when {
                pathStr.contains("PropRef(") -> {
                    val match = Regex("PropRef\\(([^)]+)\\)").find(pathStr)
                    match?.groupValues?.get(1) ?: pathStr
                }
                else -> pathStr
            }
        }
    } catch (e: Exception) {
        // Если dataPath недоступен, используем улучшенный fallback
        val pathStr = this.path.toString()
        when {
            pathStr.contains("PropRef(") -> {
                val match = Regex("PropRef\\(([^)]+)\\)").find(pathStr)
                match?.groupValues?.get(1) ?: pathStr
            }
            else -> pathStr
        }
    }.ifBlank { "field" }
}

fun <T> Validation<T>.validateToValidated(value: T): Validated<T> =
    when (val res = this(value)) {
        is Valid -> Validated.Ok(res.value)
        is Invalid ->
            Validated.Fail(
                value,
                res.errors.map { FieldError(it.pathAsString(), it.message) }
            )
    }

fun <T> T.validateWith(validator: Validation<T>): Validated<T> = validator.validateToValidated(this)

suspend inline fun <T, R> T.validateAndThen(
    validator: Validation<T>,
    block: suspend (T) -> R
): R =
    when (val v = validator.validateToValidated(this)) {
        is Validated.Ok -> block(v.value)
        is Validated.Fail -> throw ValidationException(errors = v.errors)
    }

suspend fun <T> T.validateOrThrow(validator: Validation<T>): T = validateAndThen(validator) { it }
