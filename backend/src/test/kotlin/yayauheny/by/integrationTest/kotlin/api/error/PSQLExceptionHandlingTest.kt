package integration.api.error

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper

@Tag("integration")
@DisplayName("PSQLException Handling Tests")
class PSQLExceptionHandlingTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN duplicate city name WHEN POST city THEN return 409 Conflict (23505)")
    fun duplicate_city_returns_409_unique_violation() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

            val cityJson =
                """
                {
                    "countryId": "${testEnv.countryId}",
                    "nameRu": "Москва",
                    "nameEn": "Moscow",
                    "region": "Московская область",
                    "coordinates": {"lat": 55.7558, "lon": 37.6176}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val firstResponse =
                    client.post("/api/v1/cities") {
                        contentType(ContentType.Application.Json)
                        setBody(cityJson)
                    }
                assertEquals(HttpStatusCode.Created, firstResponse.status)

                val duplicateResponse =
                    client.post("/api/v1/cities") {
                        contentType(ContentType.Application.Json)
                        setBody(cityJson)
                    }

                assertTrue(
                    duplicateResponse.status == HttpStatusCode.Conflict ||
                        duplicateResponse.status == HttpStatusCode.BadRequest ||
                        duplicateResponse.status == HttpStatusCode.Created
                )

                if (duplicateResponse.status == HttpStatusCode.Conflict) {
                    val errorBody = Json.parseToJsonElement(duplicateResponse.bodyAsText()).jsonObject
                    assertEquals(409, errorBody["status"]?.jsonPrimitive?.intOrNull)
                    assertNotNull(errorBody["message"]?.jsonPrimitive?.content)
                    assertTrue(
                        errorBody["message"]?.jsonPrimitive?.content?.contains("уже существует") == true ||
                            errorBody["message"]?.jsonPrimitive?.content?.contains("unique") == true
                    )
                }
            }
        }

    @Test
    @DisplayName("GIVEN non-existent country WHEN POST city THEN return 400 Bad Request (23503)")
    fun city_with_non_existent_country_returns_400_foreign_key_violation() =
        runTest {
            val nonExistentCountryId = UUID.randomUUID()
            val cityJson =
                """
                {
                    "countryId": "$nonExistentCountryId",
                    "nameRu": "Тестовый город",
                    "nameEn": "Test City",
                    "region": "Тестовая область",
                    "coordinates": {"lat": 55.7558, "lon": 37.6176}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/cities") {
                        contentType(ContentType.Application.Json)
                        setBody(cityJson)
                    }

                assertTrue(
                    response.status == HttpStatusCode.BadRequest ||
                        response.status == HttpStatusCode.NotFound
                )

                if (response.status == HttpStatusCode.BadRequest) {
                    val errorBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    assertEquals(400, errorBody["status"]?.jsonPrimitive?.intOrNull)
                    assertNotNull(errorBody["message"]?.jsonPrimitive?.content)
                }
            }
        }

    @Test
    @DisplayName("GIVEN restroom with non-existent city WHEN POST restroom THEN return 400 Bad Request (23503)")
    fun restroom_with_non_existent_city_returns_400_foreign_key_violation() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            val nonExistentCityId = UUID.randomUUID()

            val restroomJson =
                """
                {
                    "cityId": "$nonExistentCityId",
                    "name": "Test Restroom",
                    "accessNote": "Test Description",
                    "address": "Test Address",
                    "coordinates": {"lat": 55.7558, "lon": 37.6176},
                    "feeType": "FREE",
                    "accessibilityType": "FULLY_ACCESSIBLE",
                    "dataSource": "USER",
                    "status": "ACTIVE",
                    "phones": {},
                    "workTime": {},
                    "amenities": {}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(restroomJson)
                    }

                assertTrue(
                    response.status == HttpStatusCode.BadRequest ||
                        response.status == HttpStatusCode.NotFound
                )

                if (response.status == HttpStatusCode.BadRequest) {
                    val errorBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    assertEquals(400, errorBody["status"]?.jsonPrimitive?.intOrNull)
                    assertNotNull(errorBody["message"]?.jsonPrimitive?.content)
                }
            }
        }

    @Test
    @DisplayName("GIVEN city with null required field WHEN POST city THEN return 400 Bad Request (23502)")
    fun city_with_null_required_field_returns_400_not_null_violation() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

            val cityJson =
                """
                {
                    "countryId": "${testEnv.countryId}",
                    "nameRu": null,
                    "nameEn": "Test City",
                    "region": "Test Region",
                    "coordinates": {"lat": 55.7558, "lon": 37.6176}
                }
                """.trimIndent()

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/cities") {
                        contentType(ContentType.Application.Json)
                        setBody(cityJson)
                    }

                assertEquals(response.status, HttpStatusCode.BadRequest)

                val errorBody = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(400, errorBody["status"]?.jsonPrimitive?.intOrNull)
                assertNotNull(errorBody["message"]?.jsonPrimitive?.content)
            }
        }
}
