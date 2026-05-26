package yayauheny.by.importing.provider.osm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun JsonObject.readTags(): Map<String, String>? {
    val tagsElement = this["tags"] ?: return null
    val tagsObject =
        when (tagsElement) {
            is JsonObject -> tagsElement
            else ->
                tagsElement.jsonPrimitive.contentOrNull
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                    ?.let { rawTags -> runCatching { Json.parseToJsonElement(rawTags).jsonObject }.getOrNull() }
        } ?: return null

    return tagsObject.entries
        .mapNotNull { (key, value) ->
            value.jsonPrimitive.contentOrNull
                ?.trim()
                ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                ?.let { key to it }
        }.toMap()
}
