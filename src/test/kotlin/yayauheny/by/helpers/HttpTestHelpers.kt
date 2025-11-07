package yayauheny.by.helpers

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.time.Instant
import java.util.UUID
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import yayauheny.by.util.InstantSerializer
import yayauheny.by.util.UUIDSerializer
import yayauheny.by.common.errors.ErrorResponse

val testJson =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = false
        serializersModule =
            SerializersModule {
                contextual(UUID::class, UUIDSerializer)
                contextual(Instant::class, InstantSerializer)
            }
    }

fun HttpResponse.assertJsonContentType() {
    val contentType = this.headers["Content-Type"]
    assertTrue(
        contentType?.contains("application/json") == true,
        "Response should have JSON content type, got: $contentType"
    )
}

suspend fun HttpClient.testGet(
    path: String,
    query: Map<String, String> = emptyMap()
): HttpResponse =
    get(path) {
        url { query.forEach { (k, v) -> parameters.append(k, v) } }
        headers.append("Accept", ContentType.Application.Json.toString())
    }

suspend fun HttpClient.testPost(
    path: String,
    jsonBody: String
): HttpResponse =
    post(path) {
        contentType(ContentType.Application.Json)
        setBody(jsonBody)
    }

suspend fun HttpResponse.parseErrorResponse(): ErrorResponse {
    val body = this.bodyAsText()
    return yayauheny.by.helpers.testJson
        .decodeFromString<ErrorResponse>(body)
}

fun ErrorResponse.assertHasFieldError(
    field: String,
    expectedMessage: String? = null
) {
    assertTrue(
        this.errors?.any { it.field == field } == true,
        "Error response should contain field error for '$field', but got: ${this.errors}"
    )

    if (expectedMessage != null) {
        val fieldError = this.errors?.find { it.field == field }
        assertTrue(
            fieldError?.message?.contains(expectedMessage) == true,
            "Field error for '$field' should contain message '$expectedMessage', but got: '${fieldError?.message}'"
        )
    }
}

fun ErrorResponse.assertHasValidationErrors() {
    assertTrue(
        this.errors != null && this.errors.isNotEmpty(),
        "Error response should contain validation errors, but got: ${this.errors}"
    )
}
