package yayauheny.by.helpers

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.time.Instant
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

suspend fun HttpClient.testPut(
    path: String,
    jsonBody: String
): HttpResponse =
    put(path) {
        contentType(ContentType.Application.Json)
        setBody(jsonBody)
    }

suspend fun HttpClient.testDelete(path: String): HttpResponse =
    delete(path) {
        headers.append("Accept", ContentType.Application.Json.toString())
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

/**
 * Asserts that the response has JSON content type.
 */
fun HttpResponse.assertJsonContentType() {
    val contentType = this.headers["Content-Type"]
    assertTrue(
        contentType?.contains("application/json") == true,
        "Expected Content-Type to contain 'application/json', but got: $contentType"
    )
}

/**
 * Asserts that the HTTP response has the expected status code and JSON content type.
 * Provides clear error messages if assertions fail.
 */
fun HttpResponse.assertStatusAndJsonContent(
    expectedStatus: HttpStatusCode,
    message: String? = null
) {
    assertEquals(
        expectedStatus,
        this.status,
        message ?: "Expected status $expectedStatus but got ${this.status}"
    )
    this.assertJsonContentType()
}

/**
 * Asserts that the response body contains the specified text.
 * Provides clear error message with actual body content.
 */
suspend fun HttpResponse.assertBodyContains(
    expectedText: String,
    message: String? = null
) {
    val body = this.bodyAsText()
    assertTrue(
        body.contains(expectedText),
        message ?: "Response body should contain '$expectedText', but got: ${body.take(200)}"
    )
}

/**
 * Asserts that the response body contains all specified texts.
 */
suspend fun HttpResponse.assertBodyContainsAll(
    vararg expectedTexts: String,
    message: String? = null
) {
    val body = this.bodyAsText()
    expectedTexts.forEach { expectedText ->
        assertTrue(
            body.contains(expectedText),
            message ?: "Response body should contain '$expectedText', but got: ${body.take(200)}"
        )
    }
}

/**
 * Creates a JSON string for city creation request.
 */
fun createCityJson(
    countryId: UUID,
    nameRu: String,
    nameEn: String,
    region: String? = null,
    lat: Double = 55.7558,
    lon: Double = 37.6176
): String {
    val json =
        buildJsonObject {
            put("countryId", countryId.toString())
            put("nameRu", nameRu)
            put("nameEn", nameEn)
            if (region != null) {
                put("region", region)
            }
            put(
                "coordinates",
                buildJsonObject {
                    put("lat", lat)
                    put("lon", lon)
                }
            )
        }
    return json.toString()
}

/**
 * Creates a JSON string for country creation request.
 */
fun createCountryJson(
    nameRu: String,
    nameEn: String,
    code: String
): String {
    val json =
        buildJsonObject {
            put("nameRu", nameRu)
            put("nameEn", nameEn)
            put("code", code)
        }
    return json.toString()
}

/**
 * Creates a JSON string for city update request.
 */
fun createCityUpdateJson(dto: yayauheny.by.model.city.CityUpdateDto): String {
    val json =
        buildJsonObject {
            put("countryId", dto.countryId.toString())
            put("nameRu", dto.nameRu)
            put("nameEn", dto.nameEn)
            if (dto.region != null) {
                put("region", dto.region)
            }
            put(
                "coordinates",
                buildJsonObject {
                    put("lat", dto.coordinates.lat)
                    put("lon", dto.coordinates.lon)
                }
            )
        }
    return json.toString()
}

/**
 * Creates a JSON string for city creation request from DTO.
 */
fun createCityJson(dto: yayauheny.by.model.city.CityCreateDto): String {
    val json =
        buildJsonObject {
            put("countryId", dto.countryId.toString())
            put("nameRu", dto.nameRu)
            put("nameEn", dto.nameEn)
            if (dto.region != null) {
                put("region", dto.region)
            }
            put(
                "coordinates",
                buildJsonObject {
                    put("lat", dto.coordinates.lat)
                    put("lon", dto.coordinates.lon)
                }
            )
        }
    return json.toString()
}

/**
 * Creates a JSON string for restroom creation request.
 */
fun createRestroomJson(
    cityId: UUID? = null,
    name: String? = null,
    address: String = "Test Address",
    lat: Double = 55.7558,
    lon: Double = 37.6176,
    status: String = "ACTIVE",
    feeType: String = "FREE",
    accessibilityType: String = "UNISEX",
    dataSource: String = "MANUAL",
    amenities: JsonObject = buildJsonObject {}
): String {
    val json =
        buildJsonObject {
            if (cityId != null) {
                put("cityId", cityId.toString())
            }
            if (name != null) {
                put("name", name)
            }
            put("address", address)
            put("status", status)
            put("feeType", feeType)
            put("accessibilityType", accessibilityType)
            put("dataSource", dataSource)
            put(
                "coordinates",
                buildJsonObject {
                    put("lat", lat)
                    put("lon", lon)
                }
            )
            put("amenities", amenities)
            put("phones", buildJsonObject {})
            put("workTime", buildJsonObject {})
        }
    return json.toString()
}

/**
 * Creates a JSON string for restroom creation request from TestRestroomData.
 */
fun createRestroomJsonFromTestData(
    testData: yayauheny.by.helpers.TestRestroomData,
    cityId: java.util.UUID? = null
): String {
    val json =
        buildJsonObject {
            if (cityId != null) {
                put("cityId", cityId.toString())
            }
            if (testData.name != null) {
                put("name", testData.name)
            }
            if (testData.accessNote != null) {
                put("accessNote", testData.accessNote)
            }
            put("address", testData.address)
            put("status", testData.status.name)
            put("feeType", testData.feeType.name)
            put("accessibilityType", testData.accessibilityType.name)
            put("dataSource", testData.dataSource.name)
            put(
                "coordinates",
                buildJsonObject {
                    put("lat", testData.lat)
                    put("lon", testData.lon)
                }
            )
            put("amenities", testData.amenities)
            // Используем пустые объекты если phones/workTime null, как в unit тестах
            put("phones", testData.phones ?: buildJsonObject {})
            put("workTime", testData.workTime ?: buildJsonObject {})
        }
    return json.toString()
}
