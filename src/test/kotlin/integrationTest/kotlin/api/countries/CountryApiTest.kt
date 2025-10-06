package integration.api.countries

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import support.helpers.assertJsonContentType
import support.helpers.testGet
import support.helpers.testPost

@Tag("integration")
class CountryApiTest : BaseIntegrationTest() {
    @Nested
    @DisplayName("GET /api/v1/countries")
    inner class GetAllCountries {
        @Test
        @DisplayName("GIVEN countries endpoint WHEN GET /api/v1/countries THEN return paginated response")
        fun countries_endpoint_returns_paginated_response() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/countries")

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"content\""))
                    assertTrue(body.contains("\"totalElements\""))
                }
            }

        @Test
        @DisplayName("GIVEN pagination parameters WHEN GET countries THEN return correct pagination")
        fun countries_with_pagination_parameters() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/countries", mapOf("page" to "0", "size" to "10"))

                    assertEquals(HttpStatusCode.OK, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"page\":0"))
                    assertTrue(body.contains("\"size\":10"))
                }
            }
    }

    @Nested
    @DisplayName("POST /api/v1/countries")
    inner class CreateCountry {
        @Test
        @DisplayName("GIVEN valid country data WHEN POST country THEN return created country")
        fun create_country_with_valid_data() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val validJson = """{"nameRu": "Тестовая страна", "nameEn": "Test Country", "code": "TC"}"""
                    val response = client.testPost("/api/v1/countries", validJson)

                    assertEquals(HttpStatusCode.Created, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"id\""))
                    assertTrue(body.contains("\"code\""))
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/countries/{id}")
    inner class GetCountryById {
        @Test
        @DisplayName("GIVEN valid UUID WHEN GET country by ID THEN return valid response")
        fun get_country_by_valid_id() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val testUuid = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/countries/$testUuid")
                    assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
                    response.assertJsonContentType()
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/countries/code/{code}")
    inner class GetCountryByCode {
        @Test
        @DisplayName("GIVEN valid country code WHEN GET country by code THEN return valid response")
        fun get_country_by_valid_code() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/countries/code/US")
                    assertTrue(response.status == HttpStatusCode.OK || response.status == HttpStatusCode.NotFound)
                    response.assertJsonContentType()
                }
            }
    }

    @Nested
    @DisplayName("Negative Test Cases")
    inner class NegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/countries/{id} with invalid UUID returns 400")
        fun get_country_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/countries/invalid-uuid")
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("GET /api/v1/countries/{id} with non-existing UUID returns 404")
        fun get_country_non_existing_uuid() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/countries/$nonExistentId")
                    assertEquals(HttpStatusCode.NotFound, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("not found"))
                }
            }

        @Test
        @DisplayName("GET /api/v1/countries/code/{code} with non-existing code returns 404")
        fun get_country_non_existing_code() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/countries/code/XX")
                    assertEquals(HttpStatusCode.NotFound, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("not found"))
                }
            }

        @Test
        @DisplayName("POST /api/v1/countries with invalid JSON returns 400")
        fun create_country_invalid_json() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testPost("/api/v1/countries", """{"invalid": "data"}""")
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("POST /api/v1/countries with missing required fields returns 400")
        fun create_country_missing_required_fields() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val incompleteJson = """{"nameRu": "Тестовая страна"}"""
                    val response = client.testPost("/api/v1/countries", incompleteJson)
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("POST /api/v1/countries with too long code returns 400")
        fun create_country_too_long_code() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val tooLongCodeJson =
                        """
                        {
                            "nameRu": "Тестовая страна",
                            "nameEn": "Test Country",
                            "code": "TOOLONGCODE"
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/countries", tooLongCodeJson)
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("\"errors\""))
                }
            }

        @Test
        @DisplayName("POST /api/v1/countries with duplicate code returns 409")
        fun create_country_duplicate_code() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val firstCountryJson =
                        """
                        {
                            "nameRu": "Первая страна",
                            "nameEn": "First Country",
                            "code": "FC"
                        }
                        """.trimIndent()
                    val firstResponse = client.testPost("/api/v1/countries", firstCountryJson)
                    assertEquals(HttpStatusCode.Created, firstResponse.status)

                    val duplicateCodeJson =
                        """
                        {
                            "nameRu": "Вторая страна",
                            "nameEn": "Second Country",
                            "code": "FC"
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/countries", duplicateCodeJson)
                    assertEquals(HttpStatusCode.Conflict, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("already exists"))
                }
            }
    }
}
