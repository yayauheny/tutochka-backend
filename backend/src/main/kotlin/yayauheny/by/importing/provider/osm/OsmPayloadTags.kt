package yayauheny.by.importing.provider.osm

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

private val mapper = jacksonObjectMapper()

internal fun JsonNode.readTags(): Map<String, String>? {
    val tagsElement = this.get("tags") ?: return null
    val tagsObject =
        when {
            tagsElement.isObject -> tagsElement
            tagsElement.isTextual -> {
                tagsElement.asText()
                    .trim()
                    .takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
                    ?.let { rawTags -> runCatching { mapper.readTree(rawTags) }.getOrNull()?.takeIf { it.isObject } }
            }
            else -> null
        } ?: return null

    val map = mutableMapOf<String, String>()
    val fields = tagsObject.fields()
    while (fields.hasNext()) {
        val entry = fields.next()
        entry.value.takeIf { it.isTextual }?.asText()
            ?.trim()
            ?.takeIf { it.isNotEmpty() && !it.equals("null", ignoreCase = true) }
            ?.let { map[entry.key] = it }
    }
    return map
}
