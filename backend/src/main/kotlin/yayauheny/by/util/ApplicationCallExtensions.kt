package yayauheny.by.util

import io.ktor.http.Headers
import io.ktor.http.Parameters
import java.math.BigDecimal
import java.math.RoundingMode
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

const val HEADER_IMPORT_PROVIDER = "Import-Provider"
const val HEADER_IMPORT_PAYLOAD_TYPE = "Import-Payload-Type"
const val HEADER_IMPORT_CITY_ID = "Import-City-Id"

data class ImportHeaders(
    val provider: ImportProvider,
    val payloadType: ImportPayloadType,
    val cityId: UUID?
)

fun Headers.getImportHeaders(): ImportHeaders {
    val providerRaw =
        getRequestHeader(HEADER_IMPORT_PROVIDER)
            ?: throw ValidationException(FieldError(HEADER_IMPORT_PROVIDER, "Header is required"))
    val payloadTypeRaw =
        getRequestHeader(HEADER_IMPORT_PAYLOAD_TYPE)
            ?: throw ValidationException(FieldError(HEADER_IMPORT_PAYLOAD_TYPE, "Header is required"))
    val cityIdRaw = getRequestHeader(HEADER_IMPORT_CITY_ID)

    val provider =
        runCatching { ImportProvider.valueOf(providerRaw) }.getOrElse {
            throw ValidationException(FieldError(HEADER_IMPORT_PROVIDER, "Invalid value: $providerRaw"))
        }
    val payloadType =
        runCatching { ImportPayloadType.valueOf(payloadTypeRaw) }.getOrElse {
            throw ValidationException(FieldError(HEADER_IMPORT_PAYLOAD_TYPE, "Invalid value: $payloadTypeRaw"))
        }
    val cityId =
        when {
            cityIdRaw.isNullOrBlank() -> null
            else ->
                runCatching { UUID.fromString(cityIdRaw) }.getOrElse {
                    throw ValidationException(FieldError(HEADER_IMPORT_CITY_ID, "Invalid UUID: $cityIdRaw"))
                }
        }
    return ImportHeaders(provider, payloadType, cityId)
}

fun Parameters.toPaginationRequest(
    defaultSize: Int = ApiConstants.DEFAULT_PAGE_SIZE,
    maxSize: Int = ApiConstants.MAX_PAGE_SIZE
): PaginationRequest {
    val page = this["page"]?.toIntOrNull()?.coerceAtLeast(0) ?: ApiConstants.DEFAULT_PAGE
    val size = this["size"]?.toIntOrNull()?.coerceIn(1, maxSize) ?: defaultSize
    val sort = this["sort"]

    val direction =
        this["direction"]?.let {
            runCatching { SortDirection.valueOf(it.uppercase()) }.getOrDefault(SortDirection.ASC)
        } ?: SortDirection.ASC

    val filters =
        this["filters"]
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

fun Parameters.getUuidFromPath(paramName: String): UUID {
    val uuidString = this[paramName] ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
    return try {
        UUID.fromString(uuidString)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Неверный формат UUID для параметра $paramName: $uuidString", e)
    }
}

fun Parameters.getStringFromPath(paramName: String): String {
    return this[paramName] ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
}

fun Parameters.getDoubleFromQuery(paramName: String): Double {
    val value =
        this[paramName]
            ?: throw IllegalArgumentException("Отсутствует обязательный параметр: $paramName")
    return value.toDoubleOrNull()
        ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является числом")
}

fun Parameters.getIntFromQuery(
    paramName: String,
    default: Int? = null
): Int? {
    val value = this[paramName]
    return if (value == null) {
        default
    } else {
        value.toIntOrNull()
            ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является целым числом")
    }
}

fun Parameters.getBooleanFromQuery(
    paramName: String,
    default: Boolean = false
): Boolean {
    val value = this[paramName]
    return if (value == null) {
        default
    } else {
        value.toBooleanStrictOrNull()
            ?: throw IllegalArgumentException("Неверный формат параметра $paramName: '$value' не является булевым значением (true/false)")
    }
}

fun Headers.getRequestHeader(name: String): String? = this[name]?.trim()?.takeIf { it.isNotEmpty() }

fun Double.toBigDecimalRounded(): BigDecimal = BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP)
