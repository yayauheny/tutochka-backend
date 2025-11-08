package yayauheny.by.common.query

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

object FieldParsers {
    val string: (String) -> String? = { it }
    val int: (String) -> Int? = { it.toIntOrNull() }
    val long: (String) -> Long? = { it.toLongOrNull() }
    val double: (String) -> Double? = { it.toDoubleOrNull() }
    val boolean: (String) -> Boolean? = { it.toBooleanStrictOrNull() }
    val uuid: (String) -> UUID? = { runCatching { UUID.fromString(it) }.getOrNull() }
    val instant: (String) -> Instant? = { runCatching { Instant.parse(it) }.getOrNull() }
    val dateTime: (String) -> LocalDateTime? =
        { runCatching { LocalDateTime.parse(it) }.getOrNull() }
    val date: (String) -> LocalDate? = { runCatching { LocalDate.parse(it) }.getOrNull() }

    inline fun <reified T : Enum<T>> enum(): (String) -> T? =
        {
            runCatching { enumValueOf<T>(it.uppercase()) }.getOrNull()
        }
}
