package yayauheny.by.service.validation

import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import io.konform.validation.ValidationError
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException

/**
 * Извлекает путь поля из ValidationError как строку.
 *
 * Konform использует внутреннее представление путей через reflection и PropRef.
 * Эта функция пытается извлечь читаемое имя поля для использования в FieldError.
 *
 * Алгоритм:
 * 1. Пытается получить dataPath через reflection (внутреннее поле konform)
 * 2. Если не удалось, парсит строковое представление path, извлекая PropRef(...)
 * 3. В качестве fallback возвращает "field"
 *
 * @return читаемое имя поля (например, "nameRu", "coordinates.lat")
 */
fun ValidationError.pathAsString(): String {
    return try {
        // Попытка получить dataPath через reflection (внутреннее поле konform)
        val dataPath = this::class.java.getDeclaredField("dataPath")
        dataPath.isAccessible = true
        val pathValue = dataPath.get(this) as? List<*>
        if (pathValue != null && pathValue.isNotEmpty()) {
            pathValue.joinToString(".") { it.toString() }
        } else {
            parsePathFromString()
        }
    } catch (e: Exception) {
        // Fallback: парсинг строкового представления
        parsePathFromString()
    }.ifBlank { "field" }
}

private fun ValidationError.parsePathFromString(): String {
    val pathStr = this.path.toString()
    return when {
        pathStr.contains("PropRef(") -> {
            // PropRef - внутренний класс konform для property references
            // Извлекаем имя свойства из строки вида "PropRef(nameRu)"
            val match = Regex("PropRef\\(([^)]+)\\)").find(pathStr)
            match?.groupValues?.get(1) ?: pathStr
        }
        else -> pathStr
    }
}

/**
 * Преобразует результат валидации konform в стандартный Result<T>.
 *
 * @param value исходное значение для валидации
 * @return Result.success(value) если валидация прошла успешно,
 *         Result.failure(ValidationException) если есть ошибки валидации
 */
fun <T> Validation<T>.validateToResult(value: T): Result<T> =
    when (val res = this(value)) {
        is Valid -> Result.success(res.value)
        is Invalid ->
            Result.failure(
                ValidationException(
                    errors = res.errors.map { FieldError(it.pathAsString(), it.message) }
                )
            )
    }

/**
 * Валидирует значение с помощью указанного валидатора и возвращает Result<T>.
 *
 * @param validator валидатор для применения
 * @return Result.success(value) если валидация прошла успешно,
 *         Result.failure(ValidationException) если есть ошибки валидации
 */
fun <T> T.validateWith(validator: Validation<T>): Result<T> = validator.validateToResult(this)

/**
 * Валидирует значение и выполняет блок кода только при успешной валидации.
 * Возвращает Result<R> для дальнейшей обработки в цепочках Result.
 *
 * @param validator валидатор для применения
 * @param block функция, выполняемая с валидированным значением
 * @return Result.success(block result) если валидация и выполнение блока прошли успешно,
 *         Result.failure с ValidationException или исключением из блока
 */
suspend inline fun <T, R> T.validateAndThen(
    validator: Validation<T>,
    crossinline block: suspend (T) -> R
): Result<R> =
    validateWith(validator)
        .mapCatching { validValue -> block(validValue) }

/**
 * Валидирует значение и бросает ValidationException при ошибке валидации.
 * Используется в контроллерах и других местах, где исключения обрабатываются централизованно.
 *
 * @param validator валидатор для применения
 * @return валидированное значение
 * @throws ValidationException если валидация не прошла
 */
suspend fun <T> T.validateOrThrow(validator: Validation<T>): T = validateAndThen(validator) { it }.getOrThrow()
