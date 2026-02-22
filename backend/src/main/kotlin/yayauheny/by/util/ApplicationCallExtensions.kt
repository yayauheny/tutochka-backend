package yayauheny.by.util

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import java.util.UUID
import yayauheny.by.common.errors.FieldError
import yayauheny.by.common.errors.ValidationException
import yayauheny.by.common.query.FilterCriteria
import yayauheny.by.common.query.FilterOperator
import yayauheny.by.common.query.PaginationRequest
import yayauheny.by.common.query.SortDirection
import yayauheny.by.config.ApiConstants
import yayauheny.by.model.enums.ImportPayloadType
import yayauheny.by.model.enums.ImportProvider

const val HEADER_IMPORT_PROVIDER = "X-Import-Provider"
const val HEADER_IMPORT_PAYLOAD_TYPE = "X-Import-Payload-Type"
const val HEADER_IMPORT_CITY_ID = "X-Import-City-Id"

data class ImportHeaders(
    val provider: ImportProvider,
    val payloadType: ImportPayloadType,
    val cityId: UUID?
)

fun ApplicationCall.getImportHeaders(): ImportHeaders {
    val providerRaw =
        request.header(HEADER_IMPORT_PROVIDER)
            ?: throw ValidationException(listOf(FieldError(HEADER_IMPORT_PROVIDER, "Header is required")))
    val payloadTypeRaw =
        request.header(HEADER_IMPORT_PAYLOAD_TYPE)
            ?: throw ValidationException(listOf(FieldError(HEADER_IMPORT_PAYLOAD_TYPE, "Header is required")))
    val cityIdRaw = request.header(HEADER_IMPORT_CITY_ID)

    val provider =
        runCatching { ImportProvider.valueOf(providerRaw) }.getOrElse {
            throw ValidationException(listOf(FieldError(HEADER_IMPORT_PROVIDER, "Invalid value: $providerRaw")))
        }
    val payloadType =
        runCatching { ImportPayloadType.valueOf(payloadTypeRaw) }.getOrElse {
            throw ValidationException(listOf(FieldError(HEADER_IMPORT_PAYLOAD_TYPE, "Invalid value: $payloadTypeRaw")))
        }
    val cityId =
        when {
            cityIdRaw.isNullOrBlank() -> null
            else ->
                runCatching { UUID.fromString(cityIdRaw) }.getOrElse {
                    throw ValidationException(listOf(FieldError(HEADER_IMPORT_CITY_ID, "Invalid UUID: $cityIdRaw")))
                }
        }
    return ImportHeaders(provider, payloadType, cityId)
}

fun ApplicationCall.toPaginationRequest(
    defaultSize: Int = ApiConstants.DEFAULT_PAGE_SIZE,
    maxSize: Int = ApiConstants.MAX_PAGE_SIZE
): PaginationRequest {
    val params = request.queryParameters

    val page = params["page"]?.toIntOrNull()?.coerceAtLeast(0) ?: ApiConstants.DEFAULT_PAGE
    val size = params["size"]?.toIntOrNull()?.coerceIn(1, maxSize) ?: defaultSize
    val sort = params["sort"]

    val direction =
        params["direction"]?.let {
            runCatching { SortDirection.valueOf(it.uppercase()) }.getOrDefault(SortDirection.ASC)
        } ?: SortDirection.ASC

    val filters =
        params["filters"]
            ?.split(ApiConstants.FILTER_DELIMITER)
            ?.mapNotNull { raw ->
                val parts = raw.split(ApiConstants.FILTER_VALUE_DELIMITER)
                if (parts.size == ApiConstants.FILTER_PARTS_COUNT) {
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

fun ApplicationCall.getBooleanFromQuery(
    paramName: String,
    default: Boolean = false
): Boolean {
    val value = request.queryParameters[paramName]
    return if (value == null) {
        default
    } else {
        value.toBooleanStrictOrNull()
            ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является булевым значением (true/false)")
    }
}

fun ApplicationCall.createPaginationFromQuery(
    defaultSize: Int = ApiConstants.DEFAULT_PAGE_SIZE,
    maxSize: Int = ApiConstants.MAX_PAGE_SIZE
): PaginationRequest = toPaginationRequest(defaultSize, maxSize)
