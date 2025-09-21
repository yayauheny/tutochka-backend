package yayauheny.by.util

import io.ktor.server.application.ApplicationCall
import java.util.UUID
import yayauheny.by.model.PaginationDto

fun ApplicationCall.createPaginationFromQuery(): PaginationDto {
    val page = request.queryParameters["page"]?.toIntOrNull()
    val size = request.queryParameters["size"]?.toIntOrNull()
    val sort = request.queryParameters["sort"]
    return createPaginationFromParams(page, size, sort)
}

fun ApplicationCall.getUuidFromPath(paramName: String): UUID {
    val uuidString = parameters[paramName] ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
    return try {
        UUID.fromString(uuidString)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Неверный формат UUID для параметра $paramName: $uuidString", e)
    }
}

fun ApplicationCall.getStringFromPath(paramName: String): String {
    return parameters[paramName] ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
}

fun ApplicationCall.getDoubleFromQuery(paramName: String): Double {
    val value =
        request.queryParameters[paramName]
            ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
    return value.toDoubleOrNull()
        ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является числом")
}

fun ApplicationCall.getPositiveIntFromQuery(
    paramName: String,
    default: Int = 5,
    maxValue: Int = 100
): Int {
    val value = request.queryParameters[paramName]
    val intValue =
        if (value == null) {
            default
        } else {
            value.toIntOrNull()
                ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является целым числом")
        }

    if (intValue <= 0) {
        throw IllegalArgumentException("Параметр $paramName должен быть положительным числом")
    }

    return intValue.coerceAtMost(maxValue)
}

fun ApplicationCall.getIntFromQuery(
    paramName: String,
    default: Int? = null
): Int? {
    val value = request.queryParameters[paramName]
    return if (value == null) {
        default
    } else {
        value.toIntOrNull()
            ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является целым числом")
    }
}
