package yayauheny.by.util

import io.ktor.server.application.ApplicationCall
import java.util.UUID
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection

fun ApplicationCall.toPaginationRequest(
    defaultSize: Int = 10,
    maxSize: Int = 100
): PaginationRequest {
    val params = request.queryParameters

    val page = params["page"]?.toIntOrNull()?.coerceAtLeast(0) ?: 0
    val size = params["size"]?.toIntOrNull()?.coerceIn(1, maxSize) ?: defaultSize
    val sort = params["sort"]

    val direction =
        params["direction"]?.let {
            runCatching { SortDirection.valueOf(it.uppercase()) }.getOrDefault(SortDirection.ASC)
        } ?: SortDirection.ASC

    val filters =
        params["filters"]
            ?.split(",")
            ?.mapNotNull { raw ->
                val parts = raw.split(":")
                if (parts.size == 3) {
                    val field = parts[0]
                    val operator = runCatching { FilterOperator.valueOf(parts[1].uppercase()) }.getOrNull()
                    val value = parts[2]
                    if (operator != null) FilterCriteria(field, operator, value) else null
                } else {
                    null
                }
            } ?: emptyList()

    return PaginationRequest(
        filters = filters,
        sort = sort,
        direction = direction,
        page = page,
        size = size
    )
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

fun ApplicationCall.createPaginationFromQuery(
    defaultSize: Int = 10,
    maxSize: Int = 100
): PaginationRequest = toPaginationRequest(defaultSize, maxSize)
