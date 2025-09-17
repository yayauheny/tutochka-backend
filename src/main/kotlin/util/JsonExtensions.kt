package yayauheny.by.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.SerializationException

fun Map<String, Any>.toJsonObject(): JsonObject = try {
    val jsonString = Json.encodeToString(this)
    Json.parseToJsonElement(jsonString).jsonObject
} catch (e: SerializationException) {
    throw IllegalArgumentException("Failed to convert Map to JsonObject: ${e.message}", e)
}

fun JsonObject.toMap(): Map<String, Any> = try {
    val jsonString = Json.encodeToString(this)
    Json.decodeFromString<Map<String, Any>>(jsonString)
} catch (e: SerializationException) {
    throw IllegalArgumentException("Failed to convert JsonObject to Map: ${e.message}", e)
}

fun Map<String, Any>.toJsonObjectOrNull(): JsonObject? = try {
    toJsonObject()
} catch (e: Exception) {
    null
}

fun JsonObject.toMapOrNull(): Map<String, Any>? = try {
    toMap()
} catch (e: Exception) {
    null
}
