package yayauheny.by.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

fun Map<String, Any>.toJsonObject(): JsonObject {
    val jsonString = Json.encodeToString(this)
    return Json.parseToJsonElement(jsonString).jsonObject
}

fun JsonObject.toMap(): Map<String, Any> {
    val jsonString = Json.encodeToString(this)
    return Json.decodeFromString<Map<String, Any>>(jsonString)
}
