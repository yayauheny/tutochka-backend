package integration.api.restrooms

import integration.base.BaseIntegrationTest
import integration.base.KtorTestApplication
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.doubleOrNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import yayauheny.by.helpers.DatabaseTestHelper
import yayauheny.by.helpers.assertJsonContentType
import yayauheny.by.helpers.assertStatusAndJsonContent
import yayauheny.by.helpers.assertBodyContainsAll
import yayauheny.by.helpers.testGet
import yayauheny.by.helpers.testPost
import yayauheny.by.helpers.parseErrorResponse
import yayauheny.by.helpers.assertHasFieldError
import yayauheny.by.helpers.assertHasValidationErrors
import yayauheny.by.helpers.createRestroomJsonFromTestData
import yayauheny.by.model.enums.RestroomStatus

@Tag("integration")
class RestroomApiTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET with pagination THEN return paginated results")
    fun given_multiple_restrooms_when_get_with_pagination_then_return_paginated_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(name = "Test Restroom")
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "2")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                response.assertBodyContainsAll("\"content\"", "\"totalElements\"")
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET with pagination THEN return empty results")
    fun given_no_restrooms_when_get_with_pagination_then_return_empty_results() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "10")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(0, json["totalElements"]!!.jsonPrimitive.intOrNull)
            }
        }

    @Test
    @DisplayName("GIVEN existing restroom WHEN GET by valid ID THEN return restroom details")
    fun given_existing_restroom_when_get_by_valid_id_then_return_restroom_details() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            val restroomData = DatabaseTestHelper.createTestRestroomData(name = "Premium Restroom")
            val restroomId = DatabaseTestHelper.insertTestRestroom(dslContext, testEnv.cityId, restroomData)

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.get("/api/v1/restrooms/$restroomId") {
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertTrue(json.containsKey("id"))
                assertEquals("Premium Restroom", json["name"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN valid restroom data WHEN POST new restroom THEN create successfully")
    fun given_valid_restroom_data_when_post_new_restroom_then_create_successfully() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            val testData =
                DatabaseTestHelper.createTestRestroomData(
                    name = "New Restroom",
                    description = "Test restroom description",
                    address = "123 New Street",
                    phones = null, // Не добавляем phones
                    workTime = null // Не добавляем workTime
                )
            val restroomData = createRestroomJsonFromTestData(testData, testEnv.cityId)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/restrooms", restroomData)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertTrue(json.containsKey("id"))
                assertEquals("New Restroom", json["name"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN restroom with minimal valid data WHEN POST THEN create successfully")
    fun given_restroom_with_minimal_valid_data_when_post_then_create_successfully() =
        runTest {
            val testData =
                DatabaseTestHelper.createTestRestroomData(
                    name = "Minimal Restroom",
                    description = "Minimal description",
                    address = "Minimal Address",
                    phones = null,
                    workTime = null
                )
            val minimalData = createRestroomJsonFromTestData(testData)

            KtorTestApplication.withApp(dslContext) { client ->
                val response = client.testPost("/api/v1/restrooms", minimalData)

                response.assertStatusAndJsonContent(HttpStatusCode.Created)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals("Minimal Address", json["address"]?.jsonPrimitive?.content)
            }
        }

    @Test
    @DisplayName("GIVEN invalid restroom data WHEN POST THEN return error response")
    fun given_invalid_restroom_data_when_post_then_return_error_response() =
        runTest {
            val invalidData = """{"invalid": "data"}"""

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(invalidData)
                    }

                response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                assertEquals(400, json["status"]!!.jsonPrimitive.intOrNull)
            }
        }

    @Test
    @DisplayName("GIVEN existing restrooms WHEN GET nearest with valid coordinates THEN return nearest restrooms with distance")
    fun given_existing_restrooms_when_get_nearest_with_valid_coordinates_then_return_nearest_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 1",
                    lat = 55.7558,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 2",
                    lat = 55.7559,
                    lon = 37.6177
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                response.assertStatusAndJsonContent(HttpStatusCode.OK)
                val jsonArray = Json.parseToJsonElement(response.bodyAsText()).jsonArray
                assertTrue(jsonArray.isNotEmpty(), "Response should contain at least one restroom")
                val firstRestroom = jsonArray.first().jsonObject
                assertTrue(firstRestroom.containsKey("distanceMeters"), "Response should contain distanceMeters field")
                assertTrue(firstRestroom.containsKey("coordinates"), "Response should contain coordinates field")
                val coordinates = firstRestroom["coordinates"]?.jsonObject
                assertTrue(coordinates != null, "Coordinates should be an object")
                assertTrue(coordinates!!.containsKey("lat"), "Coordinates should have lat field")
                assertTrue(coordinates.containsKey("lon"), "Coordinates should have lon field")
                val names = jsonArray.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.content }
                assertTrue(names.any { it.contains("Restroom 1") || it.contains("Restroom 2") }, "Response should contain test restrooms")
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET nearest with valid coordinates THEN return empty array")
    fun given_no_restrooms_when_get_nearest_with_valid_coordinates_then_return_empty_array() =
        runTest {
            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body == "[]", "Response should be an empty array when no restrooms exist")
            }
        }

    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET nearest THEN return sorted by distance with correct distance values")
    fun given_multiple_restrooms_when_get_nearest_then_return_sorted_by_distance() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 55.7568,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Close Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Medium Restroom",
                    lat = 55.7563,
                    lon = 37.6176
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray =
                    kotlinx.serialization.json.Json
                        .decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertTrue(jsonArray.isNotEmpty(), "Should return at least one restroom")

                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    assertTrue(restroom.containsKey("coordinates"), "Each restroom should have coordinates field")
                    val coordinates = restroom["coordinates"]?.jsonObject
                    assertTrue(coordinates != null, "Coordinates should be an object")
                    assertTrue(coordinates!!.containsKey("lat"), "Coordinates should have lat field")
                    assertTrue(coordinates.containsKey("lon"), "Coordinates should have lon field")
                    assertTrue(restroom.containsKey("name"), "Each restroom should have name field")

                    val distance = restroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.jsonPrimitive?.doubleOrNull }
                val sortedDistances = distances.sorted()
                // Проверяем, что расстояния отсортированы по возрастанию (с небольшой погрешностью для double)
                distances.forEachIndexed { index, distance ->
                    if (index > 0) {
                        assertTrue(distance >= sortedDistances[index - 1] - 0.001, "Restrooms should be sorted by distance (nearest first)")
                    }
                }

                val firstRestroom = jsonArray.first()
                val firstName = firstRestroom["name"]?.toString()?.removeSurrounding("\"")
                assertTrue(firstName == "Close Restroom", "The closest restroom should be returned first")

                assertTrue(jsonArray.size <= 10, "Response size should not exceed limit")
                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms")

                val closeRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Close Restroom"
                    }
                assertNotNull(closeRestroom, "Close Restroom should be found in results")
                val closeDistance = closeRestroom!!["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(closeDistance, "Close Restroom should have distanceMeters")
                assertTrue(closeDistance!! in 5.0..20.0, "Close Restroom distance should be approximately 11m (±15m tolerance)")

                val mediumRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Medium Restroom"
                    }
                assertNotNull(mediumRestroom, "Medium Restroom should be found in results")
                val mediumDistance = mediumRestroom!!["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(mediumDistance, "Medium Restroom should have distanceMeters")
                assertTrue(mediumDistance!! in 40.0..70.0, "Medium Restroom distance should be approximately 55m (±15m tolerance)")

                val farRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Far Restroom"
                    }
                assertNotNull(farRestroom, "Far Restroom should be found in results")
                val farDistance = farRestroom!!["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                assertNotNull(farDistance, "Far Restroom should have distanceMeters")
                assertTrue(farDistance!! in 95.0..125.0, "Far Restroom distance should be approximately 111m (±15m tolerance)")

                assertTrue(closeDistance < mediumDistance, "Close Restroom should be closer than Medium Restroom")
                assertTrue(mediumDistance < farDistance, "Medium Restroom should be closer than Far Restroom")
            }
        }

    @Test
    @DisplayName("GIVEN many restrooms WHEN GET nearest with small limit THEN return only limited results")
    fun given_many_restrooms_when_get_nearest_with_small_limit_then_return_only_limited_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)
            repeat(10) { i ->
                DatabaseTestHelper.insertTestRestroom(
                    dslContext,
                    testEnv.cityId,
                    DatabaseTestHelper.createTestRestroomData(
                        name = "Restroom $i",
                        lat = 55.7558 + i * 0.001,
                        lon = 37.6176 + i * 0.001
                    )
                )
            }

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "3")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray =
                    kotlinx.serialization.json.Json
                        .decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms when limit is 3")

                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    val distance = restroom["distanceMeters"]?.jsonPrimitive?.doubleOrNull
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.jsonPrimitive?.doubleOrNull }
                val sortedDistances = distances.sorted()
                assertEquals(sortedDistances, distances, "Restrooms should be sorted by distance (nearest first)")
            }
        }

    @Test
    @DisplayName("GIVEN active and inactive restrooms WHEN GET nearest THEN return only active restrooms")
    fun given_active_and_inactive_restrooms_when_get_nearest_then_return_only_active_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(dslContext)

            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Active Restroom",
                    lat = 55.7559,
                    lon = 37.6176
                )
            )

            DatabaseTestHelper.insertTestRestroom(
                dslContext,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Inactive Restroom",
                    lat = 55.7558,
                    lon = 37.6176,
                    status = RestroomStatus.INACTIVE
                )
            )

            KtorTestApplication.withApp(dslContext) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                val jsonArray =
                    kotlinx.serialization.json.Json
                        .decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertEquals(1, jsonArray.size, "Should return only 1 restroom (the active one)")

                val returnedRestroom = jsonArray.first()
                val name = returnedRestroom["name"]?.toString()?.removeSurrounding("\"")
                assertEquals("Active Restroom", name, "Should return the active restroom, not the inactive one")

                val distanceValue = returnedRestroom["distanceMeters"]?.jsonPrimitive
                assertNotNull(distanceValue, "Active restroom should have distanceMeters")
                val distance = distanceValue?.doubleOrNull
                assertNotNull(distance, "Distance should be a valid number")
                assertTrue(distance!! >= 0.0, "Distance should be non-negative")
            }
        }

    @Nested
    @DisplayName("Negative Test Cases")
    inner class NegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with invalid UUID returns 400")
        fun get_restroom_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/invalid-uuid")
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/city/{cityId} with invalid UUID returns 400")
        fun get_restrooms_by_city_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/city/invalid-uuid")
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with non-existing UUID returns 404")
        fun get_restroom_non_existing_uuid() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/restrooms/$nonExistentId")
                    response.assertStatusAndJsonContent(HttpStatusCode.NotFound)
                    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
                    val message = json["message"]?.jsonPrimitive?.content
                    // Проверяем, что сообщение об ошибке присутствует (может быть на русском или английском)
                    assertTrue(message != null && (message.lowercase().contains("not found") || message.contains("не найден")))
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with invalid lat type returns 400")
        fun nearest_invalid_lat_type() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "abc", "lon" to "37.6"))
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with out-of-range coordinates returns 400 with validation errors")
        fun nearest_out_of_range_coordinates() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "91.0", "lon" to "37.6"))
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)

                    val errorResponse = response.parseErrorResponse()
                    errorResponse.assertHasValidationErrors()
                    // For nested coordinates validation, errors are reported on "coordinates" field
                    errorResponse.assertHasFieldError("coordinates", "не более 90 градусов")
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid JSON returns 400")
        fun post_restroom_invalid_json() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val response = client.testPost("/api/v1/restrooms", """{"invalid": "data"}""")
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid enum returns 400")
        fun post_restroom_invalid_enum() =
            runTest {
                KtorTestApplication.withApp(dslContext) { client ->
                    val invalidEnumJson =
                        """
                        {
                            "status": "ACTIVE",
                            "name": "Test Restroom",
                            "address": "Test Address",
                            "feeType": "UNKNOWN",
                            "accessibilityType": "UNISEX",
                            "coordinates": {
                                "lat": 55.7,
                                "lon": 37.6
                            },
                            "dataSource": "MANUAL",
                            "amenities": {}
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/restrooms", invalidEnumJson)
                    response.assertStatusAndJsonContent(HttpStatusCode.BadRequest)
                }
            }
    }
}
