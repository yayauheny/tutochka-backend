package integration.api.cities

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertBodyContainsAll
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.createCityJson
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost

@Tag("integration")
class CityApiTest : BaseIntegrationTest() {
    @Nested
    @DisplayName("GET /api/v1/cities/country/{countryId}")
    inner class GetCitiesByCountry {
        @Test
        @DisplayName("GIVEN valid country ID WHEN GET cities by country THEN return cities")
        fun get_cities_by_valid_country_id() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/country/${testEnv.countryId}")

                    response.assertStatusAndJsonContent(HttpStatusCode.OK)
                    response.assertBodyContainsAll("\"content\"", "\"totalElements\"")
                }
            }

        @Test
        @DisplayName("GIVEN invalid country ID WHEN GET cities by country THEN return 400")
        fun get_cities_by_invalid_country_id() =
            runTest {
                // (invalid UUID)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/country/invalid-uuid")

                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }
    }

    @Nested
    @DisplayName("GET /api/v1/cities/search")
    inner class SearchCities {
        @Test
        @DisplayName("GIVEN valid name parameter WHEN search cities THEN return results")
        fun search_cities_with_valid_name() =
            runTest {
                // (search parameter)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/search", mapOf("name" to "Test"))

                    response.assertStatusAndJsonContent(HttpStatusCode.OK)
                    response.assertBodyContainsAll("\"content\"", "\"totalElements\"")
                }
            }

        @Test
        @DisplayName("GIVEN missing name parameter WHEN search cities THEN return 400")
        fun search_cities_without_name_parameter() =
            runTest {
                // (no name parameter)

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/search")

                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    assertEquals(400, json["status"]!!.jsonPrimitive.intOrNull)
                }
            }
    }

    @Nested
    @DisplayName("E2E Test Cases")
    inner class E2ETestCases {
        // E2E tests for search functionality (real database queries)
        // E2E tests for country relationships (foreign key constraints)

        @Test
        @DisplayName("GIVEN non-existing city ID WHEN GET city by ID THEN return 404")
        fun get_city_non_existing_uuid() =
            runTest {
                val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/$nonExistentId")

                    response.assertStatusAndJsonContent(HttpStatusCode.NotFound)
                    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    // Проверяем, что сообщение об ошибке присутствует (может быть на русском или английском)
                    assertTrue(message != null && (message.lowercase().contains("not found") || message.contains("не найден")))
                }
            }

        @Test
        @DisplayName("GIVEN non-existing country ID WHEN GET cities by country THEN return empty results")
        fun get_cities_by_non_existing_country() =
            runTest {
                val nonExistentCountryId = "550e8400-e29b-41d4-a716-446655440000"

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/cities/country/$nonExistentCountryId")

                    response.assertStatusAndJsonContent(HttpStatusCode.OK)
                    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    assertEquals(0, json["totalElements"]!!.jsonPrimitive.intOrNull)
                }
            }

        @Test
        @DisplayName("GIVEN invalid countryId UUID WHEN POST city THEN return 400")
        fun create_city_invalid_country_id() =
            runTest {
                val invalidCountryIdJson =
                    """{"countryId": "invalid-uuid", "nameRu": "Тестовый город", "nameEn": "Test City", "coordinates": {"lat": 55.7558, "lon": 37.6176}}"""

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", invalidCountryIdJson)

                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("GIVEN non-existing countryId WHEN POST city THEN return 400")
        fun create_city_non_existing_country_id() =
            runTest {
                val nonExistentCountryIdJson =
                    createCityJson(
                        countryId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                        nameRu = "Тестовый город",
                        nameEn = "Test City"
                    )

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", nonExistentCountryIdJson)

                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("GIVEN invalid lat type WHEN POST city THEN return 400")
        fun create_city_invalid_lat_type() =
            runTest {
                val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
                val invalidLatJson =
                    """
                    {
                        "countryId": "${testEnv.countryId}",
                        "nameRu": "Тестовый город",
                        "nameEn": "Test City",
                        "coordinates": {
                            "lat": "north",
                            "lon": 37.6176
                        }
                    }
                    """.trimIndent()

                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/cities", invalidLatJson)

                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }
    }
}
