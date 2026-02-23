package yayauheny.by.service.import

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.jsonArray

fun JsonObject.requireString(key: String): String {
    val value = this[key]?.jsonPrimitive?.content
    if (value.isNullOrBlank()) throw InvalidImportPayload("Missing or invalid '$key'")
    return value.trim()
}

fun JsonObject.requireDouble(key: String): Double {
    val value = this[key]?.jsonPrimitive?.content?.toDoubleOrNull()
    if (value == null) throw InvalidImportPayload("Missing or invalid '$key'")
    return value
}

fun JsonObject.requireObject(key: String): JsonObject {
    val value = this[key]?.jsonObject
    if (value == null) throw InvalidImportPayload("Missing or invalid '$key'")
    return value
}

fun JsonObject.optionalString(key: String): String? =
    this[key]
        ?.jsonPrimitive
        ?.content
        ?.trim()
        ?.takeIf { it.isNotBlank() && !it.equals("null", ignoreCase = true) }

fun JsonObject.optionalArray(key: String): JsonArray? {
    val elem = this[key] ?: return null
    if (elem is JsonNull) return null
    return elem.jsonArray
}
