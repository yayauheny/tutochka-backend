package yayauheny.by.importing.dedup

import java.security.MessageDigest
import java.time.Instant
import java.time.format.DateTimeParseException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

private val mapper = jacksonObjectMapper()

object PayloadHashing {
    fun canonicalPayloadHash(payload: JsonNode): String = sha256Hex(mapper.writeValueAsString(canonicalize(payload)))

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

    private fun canonicalize(element: JsonNode): JsonNode =
        when {
            element.isArray -> {
                val array = mapper.createArrayNode()
                element.elements().forEach { array.add(canonicalize(it)) }
                array
            }
            element.isObject -> {
                val obj = mapper.createObjectNode()
                element.fields().asSequence().sortedBy { it.key }.forEach {
                    obj.set<JsonNode>(it.key, canonicalize(it.value))
                }
                obj
            }
            else -> element
        }

    private fun sha256Hex(value: String): String =
        MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
