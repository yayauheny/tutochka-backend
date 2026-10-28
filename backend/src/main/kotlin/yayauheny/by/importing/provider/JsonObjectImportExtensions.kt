package yayauheny.by.importing.provider

import com.fasterxml.jackson.databind.JsonNode
import yayauheny.by.importing.exception.InvalidImportPayload

fun String?.normalizedProviderText(): String? =
    this
        ?.trim()
        ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }

fun JsonNode.requireString(key: String): String {
    val value = this.get(key)?.takeIf { it.isTextual }?.asText()
    if (value.isNullOrBlank()) throw InvalidImportPayload("Missing or invalid '$key'")
    return value.trim()
}

fun JsonNode.requireDouble(key: String): Double {
    val value = this.get(key)?.takeIf { it.isNumber || it.isTextual }?.asDouble()
    if (value == null) throw InvalidImportPayload("Missing or invalid '$key'")
    return value
}

fun JsonNode.requireObject(key: String): JsonNode {
    val value = this.get(key)?.takeIf { it.isObject }
    if (value == null) throw InvalidImportPayload("Missing or invalid '$key'")
    return value
}

fun JsonNode.optionalString(key: String): String? =
    this.get(key)
        ?.takeIf { it.isTextual }
        ?.asText()
        .normalizedProviderText()

fun JsonNode.optionalText(): String? =
    this.takeIf { it.isTextual }
        ?.asText()
        .normalizedProviderText()

fun JsonNode.optionalObject(key: String): JsonNode? =
    this.get(key)
        ?.takeIf { it.isObject && !it.isEmpty }

fun JsonNode.optionalArray(key: String): JsonNode? {
    val elem = this.get(key) ?: return null
    if (elem.isNull || !elem.isArray) return null
    return elem
}
