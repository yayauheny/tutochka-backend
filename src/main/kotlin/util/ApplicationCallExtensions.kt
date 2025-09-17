package yayauheny.by.util

import io.ktor.server.application.ApplicationCall
import yayauheny.by.model.PaginationDto
import java.util.UUID

fun ApplicationCall.createPaginationFromQuery(): PaginationDto {
    val page = request.queryParameters["page"]?.toIntOrNull()
    val size = request.queryParameters["size"]?.toIntOrNull()
    val sort = request.queryParameters["sort"]
    return createPaginationFromParams(page, size, sort)
}

fun ApplicationCall.getUuidFromPath(paramName: String): UUID {
    return UUID.fromString(parameters[paramName])
}

fun ApplicationCall.getStringFromPath(paramName: String): String {
    return parameters[paramName] ?: throw IllegalArgumentException("Missing $paramName parameter")
}

fun ApplicationCall.getDoubleFromQuery(paramName: String): Double {
    return request.queryParameters[paramName]?.toDoubleOrNull() 
        ?: throw IllegalArgumentException("Missing or invalid $paramName parameter")
}

fun ApplicationCall.getIntFromQuery(paramName: String, default: Int = 5): Int {
    return request.queryParameters[paramName]?.toIntOrNull() ?: default
}