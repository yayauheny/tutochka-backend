package yayauheny.by.helpers

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.*

/**
 * JSON Test Helpers with DSL-style assertions.
 *
 * TODO: Keep for future use - Advanced JSON assertion utilities using DSL pattern.
 * Currently not used in tests (HttpTestHelpers is used instead).
 * This file provides:
 * - DSL-style JSON assertions via `expect()` extension function
 * - JSON path navigation (e.g., `$.a.b[0].c`)
 * - Type-safe assertions for nested JSON structures
 *
 * Consider using this for complex JSON validation scenarios where
 * HttpTestHelpers.assertBodyContains() is not sufficient.
 *
 * Example usage (when adopted):
 * ```kotlin
 * response.expect {
 *     ok().contentTypeJson()
 *     json {
 *         pathEquals("$.id", expectedId)
 *         pathExists("$.name")
 *         arraySize("$.items", 3)
 *     }
 * }
 * ```
 */
private val json = testJson // Используем общий testJson из HttpTestHelpers.kt

// ---- JSON path (поддержка $.a.b[0].c)
private fun JsonElement.getByPath(path: String): JsonElement? {
    if (path == "\$" || path.isBlank()) return this
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
            if (seg.startsWith("[")) {
                val idx = seg.removePrefix("[").removeSuffix("]").toInt()
                current = (current as? JsonArray)?.getOrNull(idx) ?: return null
            } else {
                current = (current as? JsonObject)?.get(seg) ?: return null
            }
        }
        return current
    }
    for (t in tokens) cur = step(cur, t) ?: return null
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
    private val response: HttpResponse,
    private val body: String
) {
    fun statusIs(code: HttpStatusCode) = apply { assertEquals(code, response.status) }

    fun ok() = statusIs(HttpStatusCode.OK)

    fun created() = statusIs(HttpStatusCode.Created)

    fun badRequest() = statusIs(HttpStatusCode.BadRequest)

    fun notFound() = statusIs(HttpStatusCode.NotFound)

    fun conflict() = statusIs(HttpStatusCode.Conflict)

    fun contentTypeJson() =
        apply {
            val ct = response.contentType()
            assertTrue(
                ct?.match(ContentType.Application.Json) == true,
                "Expected Content-Type application/json but was $ct"
            )
        }

    fun json(assert: JsonExpect.() -> Unit) =
        apply {
            contentTypeJson()
            val root = json.parseToJsonElement(body)
            JsonExpect(root, body).assert()
        }

    fun expectError(
        http: HttpStatusCode,
        messageContains: String? = null,
        hasErrorsArray: Boolean = false
    ) = apply {
        statusIs(http).contentTypeJson()
        val root = json.parseToJsonElement(body)
        val je = JsonExpect(root, body)
        je.pathEquals("$.status", http.value)
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
        arr!!.forEach {
            assertTrue(it is JsonObject, "Element in '$path' is not object. Body: $rawBody")
            assertEach(it.jsonObject)
        }
    }
}

suspend fun HttpResponse.expect(block: ResponseExpect.() -> Unit) {
    val body = this.bodyAsText() // кэшируем
    ResponseExpect(this, body).block()
}

suspend fun HttpResponse.assertPagination(
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
