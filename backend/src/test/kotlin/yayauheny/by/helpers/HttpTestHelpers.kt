package yayauheny.by.helpers

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromString
import yayauheny.by.common.errors.ErrorResponse

val testJson =
    Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

class MockMvcResponse internal constructor(
    private val result: MvcResult
) {
    val status: HttpStatus
        get() = HttpStatus.valueOf(result.response.status)

    fun bodyAsText(): String = result.response.contentAsString

    fun header(name: String): String? = result.response.getHeader(name)
}

fun MockMvc.testGet(
    path: String,
    query: Map<String, String> = emptyMap(),
    headers: Map<String, String> = emptyMap()
): MockMvcResponse {
    val request =
        MockMvcRequestBuilders
            .get(path)
            .accept(MediaType.APPLICATION_JSON)
            .apply {
                query.forEach { (key, value) ->
                    param(key, value)
                }
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
    return MockMvcResponse(this.perform(request).andReturn())
}

fun MockMvc.testPost(
    path: String,
    jsonBody: String,
    headers: Map<String, String> = emptyMap()
): MockMvcResponse {
    val builder =
        MockMvcRequestBuilders
            .post(path)
            .accept(MediaType.APPLICATION_JSON)
            .apply {
                if (jsonBody.isNotBlank()) {
                    contentType(MediaType.APPLICATION_JSON)
                    content(jsonBody)
                }
                headers.forEach { (key, value) ->
                    header(key, value)
                }
            }
    return MockMvcResponse(this.perform(builder).andReturn())
}

fun MockMvc.testPut(
    path: String,
    jsonBody: String
): MockMvcResponse {
    val request =
        MockMvcRequestBuilders
            .put(path)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .content(jsonBody)
    return MockMvcResponse(this.perform(request).andReturn())
}

fun MockMvc.testDelete(path: String): MockMvcResponse {
    val request =
        MockMvcRequestBuilders
            .delete(path)
            .accept(MediaType.APPLICATION_JSON)
    return MockMvcResponse(this.perform(request).andReturn())
}

fun MockMvcResponse.parseErrorResponse(): ErrorResponse =
    testJson.decodeFromString(bodyAsText())

fun ErrorResponse.assertHasFieldError(
    field: String,
    expectedMessage: String? = null
) {
    org.assertj.core.api.Assertions.assertThat(this.errors)
        .withFailMessage("Error response should contain field error for '%s', but got: %s", field, this.errors)
        .anySatisfy { error ->
            org.assertj.core.api.Assertions.assertThat(error.field).isEqualTo(field)
            if (expectedMessage != null) {
                org.assertj.core.api.Assertions.assertThat(error.message).contains(expectedMessage)
            }
        }
}

fun ErrorResponse.assertHasValidationErrors() {
    org.assertj.core.api.Assertions.assertThat(this.errors)
        .withFailMessage("Error response should contain validation errors, but got: %s", this.errors)
        .isNotNull
    org.assertj.core.api.Assertions.assertThat(this.errors).isNotEmpty
}

fun MockMvcResponse.assertJsonContentType() {
    val contentType = header(HttpHeaders.CONTENT_TYPE)
    org.assertj.core.api.Assertions.assertThat(contentType)
        .withFailMessage("Expected Content-Type to contain 'application/json', but got: %s", contentType)
        .contains("application/json")
}

fun MockMvcResponse.assertStatusAndJsonContent(
    expectedStatus: HttpStatus,
    message: String? = null
) {
    org.assertj.core.api.Assertions.assertThat(status)
        .withFailMessage(message ?: "Expected status %s but got %s", expectedStatus, status)
        .isEqualTo(expectedStatus)
    assertJsonContentType()
}

fun MockMvcResponse.assertBodyContains(
    expectedText: String,
    message: String? = null
) {
    val body = bodyAsText()
    org.assertj.core.api.Assertions.assertThat(body)
        .withFailMessage(message ?: "Response body should contain '%s', but got: %s", expectedText, body.take(200))
        .contains(expectedText)
}

fun MockMvcResponse.assertBodyContainsAll(
    vararg expectedTexts: String,
    message: String? = null
) {
    val body = bodyAsText()
    expectedTexts.forEach { expectedText ->
        org.assertj.core.api.Assertions.assertThat(body)
            .withFailMessage(message ?: "Response body should contain '%s', but got: %s", expectedText, body.take(200))
            .contains(expectedText)
    }
}

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

fun createRestroomJson(
    cityId: UUID? = null,
    name: String? = null,
    address: String = "Test Address",
    lat: Double = 55.7558,
    lon: Double = 37.6176,
    status: String = "ACTIVE",
    feeType: String = "FREE",
    accessibilityType: String = "WHEELCHAIR",
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
            put("phones", testData.phones ?: buildJsonObject {})
            put("workTime", testData.workTime ?: buildJsonObject {})
        }
    return json.toString()
}
