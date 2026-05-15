package yayauheny.by.importing.dedup

import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeParseException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

private val canonicalJson = Json { encodeDefaults = true }

object PayloadHashing {
    fun canonicalPayloadHash(payload: JsonObject): String = sha256Hex(canonicalJson.encodeToString(canonicalize(payload)))

    fun parseInstantOrNull(value: String?): Instant? =
        value
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?.let {
                try {
                    Instant.parse(it)
                } catch (_: DateTimeParseException) {
                    null
                }
            }

    private fun canonicalize(element: JsonElement): JsonElement =
        when (element) {
            is JsonArray -> JsonArray(element.map(::canonicalize))
            is JsonObject -> JsonObject(element.entries.sortedBy { it.key }.associate { it.key to canonicalize(it.value) })
            is JsonPrimitive -> element
            JsonNull -> JsonNull
        }

    private fun sha256Hex(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
