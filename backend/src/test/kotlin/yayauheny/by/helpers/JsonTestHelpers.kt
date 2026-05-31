package yayauheny.by.helpers

import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.parseToJsonElement

private val json = testJson

private fun JsonElement.getByPath(path: String): JsonElement? {
    if (path == "$" || path.isBlank()) return this
    val clean = if (path.startsWith("$.")) path.removePrefix("$.") else path
    var cur: JsonElement = this

    val tokens = Regex("""([^.]+)""").findAll(clean).map { it.value }.toList()

    fun step(
        elem: JsonElement,
        token: String
    ): JsonElement? {
        val segments = Regex("""([^\[]+)|(\[\d+])""").findAll(token).map { it.value }.toList()
        var current: JsonElement? = elem
        for (seg in segments) {
            current =
                if (seg.startsWith("[")) {
                    val idx = seg.removePrefix("[").removeSuffix("]").toInt()
                    (current as? JsonArray)?.getOrNull(idx)
                } else {
                    (current as? JsonObject)?.get(seg)
                } ?: return null
        }
        return current
    }

    for (token in tokens) {
        cur = step(cur, token) ?: return null
    }
    return cur
}

private fun JsonElement?.asString(): String? = this?.jsonPrimitive?.contentOrNull

private fun JsonElement?.asInt(): Int? = this?.jsonPrimitive?.intOrNull

private fun JsonElement?.asLong(): Long? = this?.jsonPrimitive?.longOrNull

private fun JsonElement?.asDouble(): Double? = this?.jsonPrimitive?.doubleOrNull

private fun JsonElement?.asBoolean(): Boolean? = this?.jsonPrimitive?.booleanOrNull

private fun JsonElement?.asObject(): JsonObject? = this as? JsonObject

private fun JsonElement?.asArray(): JsonArray? = this as? JsonArray

class ResponseExpect(
    private val response: MockMvcResponse,
    private val body: String
) {
    fun statusIs(code: Int) = apply { assertEquals(code, response.status.value()) }

    fun ok() = statusIs(200)

    fun created() = statusIs(201)

    fun badRequest() = statusIs(400)

    fun notFound() = statusIs(404)

    fun conflict() = statusIs(409)

    fun contentTypeJson() =
        apply {
            response.assertJsonContentType()
        }

    fun json(assert: JsonExpect.() -> Unit) =
        apply {
            contentTypeJson()
            val root = json.parseToJsonElement(body)
            JsonExpect(root, body).assert()
        }

    fun expectError(
        http: Int,
        messageContains: String? = null,
        hasErrorsArray: Boolean = false
    ) = apply {
        statusIs(http).contentTypeJson()
        val root = json.parseToJsonElement(body)
        val je = JsonExpect(root, body)
        je.pathEquals("$.status", http)
        je.pathExists("$.error")
        if (messageContains != null) je.pathContains("$.message", messageContains)
        if (hasErrorsArray) je.pathExists("$.errors")
    }

    fun rawBody(): String = body
}

class JsonExpect(
    private val root: JsonElement,
    private val rawBody: String
) {
    fun pathExists(path: String) {
        val el = root.getByPath(path)
        assertTrue(el != null, "JSON path '$path' does not exist. Body: $rawBody")
    }

    fun pathNull(path: String) {
        val el = root.getByPath(path)
        assertTrue(el == null || el is JsonNull, "JSON path '$path' expected to be null. Body: $rawBody")
    }

    fun pathEquals(
        path: String,
        expected: Any?
    ) {
        val el = root.getByPath(path)
        val fail = { actual: Any? -> "JSON path '$path' expected <$expected> but was <$actual>. Body: $rawBody" }
        when (expected) {
            null -> if (el != null && el !is JsonNull) kotlin.test.fail(fail(el))
            is Int -> assertEquals(expected, el.asInt(), fail(el?.toString()))
            is Long -> assertEquals(expected, el.asLong(), fail(el?.toString()))
            is Double -> assertEquals(expected, el.asDouble(), fail(el?.toString()))
            is Number -> {
                val actual = el.asDouble()
                assertTrue(actual != null && actual.toDouble() == expected.toDouble(), fail(actual))
            }
            is Boolean -> assertEquals(expected, el.asBoolean(), fail(el?.toString()))
            is String -> assertEquals(expected, el.asString(), fail(el?.toString()))
            else -> kotlin.test.fail("Unsupported expected type for pathEquals: ${expected::class}")
        }
    }

    fun pathContains(
        path: String,
        needle: String,
        ignoreCase: Boolean = true
    ) {
        val actual = root.getByPath(path).asString()
        assertTrue(
            actual != null && actual.contains(needle, ignoreCase),
            "JSON path '$path' did not contain '$needle'. Actual: '$actual'. Body: $rawBody"
        )
    }

    fun arraySize(
        path: String,
        expectedSize: Int
    ) {
        val arr = root.getByPath(path).asArray()
        assertTrue(arr != null, "JSON path '$path' is not an array. Body: $rawBody")
        assertEquals(expectedSize, arr!!.size, "Array '$path' size mismatch. Body: $rawBody")
    }

    fun arrayAny(
        path: String,
        predicate: (JsonObject) -> Boolean
    ) {
        val arr = root.getByPath(path).asArray()
        assertTrue(arr != null, "JSON path '$path' is not an array. Body: $rawBody")
        val ok = arr!!.any { it is JsonObject && predicate(it.jsonObject) }
        assertTrue(ok, "No element in '$path' satisfied predicate. Body: $rawBody")
    }

    fun arrayEach(
        path: String,
        assertEach: (JsonObject) -> Unit
    ) {
        val arr = root.getByPath(path).asArray()
        assertTrue(arr != null, "JSON path '$path' is not an array. Body: $rawBody")
        arr.forEach {
            assertTrue(it is JsonObject, "Element in '$path' is not object. Body: $rawBody")
            assertEach(it.jsonObject)
        }
    }
}

fun MockMvcResponse.expect(block: ResponseExpect.() -> Unit) {
    val body = this.bodyAsText()
    ResponseExpect(this, body).block()
}

fun MockMvcResponse.assertPagination(
    page: Int,
    size: Int
) {
    expect {
        json {
            pathEquals("$.page", page)
            pathEquals("$.size", size)
            pathExists("$.content")
            pathExists("$.totalElements")
        }
    }
}
