package yayauheny.by.service.validation

import io.konform.validation.Invalid
import io.konform.validation.Valid
import io.konform.validation.Validation
import io.konform.validation.ValidationError
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException

/**
 * Извлекает путь поля из ValidationError как строку, используя официальный API konform.
 *
 * Использует официальный API konform через свойство path и его метод toString().
 * Konform возвращает путь в формате "ValidationPath(PropRef(field1), PropRef(field2))",
 * поэтому мы извлекаем имена полей и соединяем их точками для читаемого формата.
 *
 * Этот подход использует публичный API (toString()), а не reflection, что делает его
 * более надежным и производительным.
 *
 * @return читаемое имя поля (например, "nameRu", "coordinates.lat")
 */
fun ValidationError.pathAsString(): String {
    val pathStr = this.path.toString()
    if (pathStr.isBlank()) return "field"

    val fieldPattern = Regex("PropRef\\(([^)]+)\\)")
    val matches = fieldPattern.findAll(pathStr)
    val fields = matches.map { it.groupValues[1] }.toList()

    return if (fields.isNotEmpty()) {
        fields.joinToString(".")
    } else {
        pathStr.ifBlank { "field" }
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
