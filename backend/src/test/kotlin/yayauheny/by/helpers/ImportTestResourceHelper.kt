package yayauheny.by.helpers

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

fun loadImportResourceAsJsonObject(resourcePath: String): JsonObject {
    val text =
        object {}
            .javaClass.classLoader
            .getResourceAsStream(resourcePath)
            ?.reader(Charsets.UTF_8)
            ?.use { it.readText() }
            ?: throw IllegalArgumentException("Resource not found: $resourcePath")
    return try {
        testJson.parseToJsonElement(text).jsonObject
    } catch (e: Exception) {
        throw IllegalStateException("Failed to parse JSON from $resourcePath: ${e.message}", e)
    }
}

fun loadImportResourceItems(resourcePath: String): List<JsonObject> {
    val root = loadImportResourceAsJsonObject(resourcePath)
    val itemsElem = root["items"] ?: throw IllegalStateException("Resource $resourcePath has no 'items' key")
    val arr = itemsElem.jsonArray
    return arr.mapNotNull { it as? JsonObject }.also { list ->
        if (list.size != arr.size) throw IllegalStateException("Resource $resourcePath: each item must be a JSON object")
    }
}
