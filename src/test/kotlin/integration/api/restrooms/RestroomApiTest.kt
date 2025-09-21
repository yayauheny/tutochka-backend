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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import support.helpers.DatabaseTestHelper
import support.helpers.assertJsonContentType
import support.helpers.testGet
import support.helpers.testPost
import support.helpers.parseErrorResponse
import support.helpers.assertHasFieldError
import support.helpers.assertHasValidationErrors

class RestroomApiTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN multiple restrooms WHEN GET with pagination THEN return paginated results")
    fun given_multiple_restrooms_when_get_with_pagination_then_return_paginated_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(name = "Test Restroom")
            )

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "2")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("\"content\""))
                assertTrue(body.contains("\"totalElements\""))
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET with pagination THEN return empty results")
    fun given_no_restrooms_when_get_with_pagination_then_return_empty_results() =
        runTest {
            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.get("/api/v1/restrooms") {
                        url {
                            parameters.append("page", "0")
                            parameters.append("size", "10")
                        }
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("\"totalElements\":0"))
            }
        }

    @Test
    @DisplayName("GIVEN existing restroom WHEN GET by valid ID THEN return restroom details")
    fun given_existing_restroom_when_get_by_valid_id_then_return_restroom_details() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            val restroomData = DatabaseTestHelper.createTestRestroomData(name = "Premium Restroom")
            val restroomId = DatabaseTestHelper.insertTestRestroom(testDatabase, testEnv.cityId, restroomData)

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.get("/api/v1/restrooms/$restroomId") {
                        headers.append("Accept", ContentType.Application.Json.toString())
                    }

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("\"id\""))
                assertTrue(body.contains("Premium Restroom"))
            }
        }

    @Test
    @DisplayName("GIVEN valid restroom data WHEN POST new restroom THEN create successfully")
    fun given_valid_restroom_data_when_post_new_restroom_then_create_successfully() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            val restroomData =
                """
                {
                    "cityId": "${testEnv.cityId}",
                    "name": "New Restroom",
                    "description": "Test restroom description",
                    "address": "123 New Street",
                    "phones": {},
                    "workTime": {},
                    "feeType": "FREE",
                    "accessibilityType": "UNISEX",
                    "lat": 55.7558,
                    "lon": 37.6176,
                    "dataSource": "MANUAL",
                    "amenities": {}
                }
                """.trimIndent()

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(restroomData)
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("\"id\""))
                assertTrue(body.contains("New Restroom"))
            }
        }

    @Test
    @DisplayName("GIVEN restroom with minimal valid data WHEN POST THEN create successfully")
    fun given_restroom_with_minimal_valid_data_when_post_then_create_successfully() =
        runTest {
            val minimalData =
                """
                {
                    "cityId": null,
                    "name": "Minimal Restroom",
                    "description": "Minimal description",
                    "address": "Minimal Address",
                    "phones": {},
                    "workTime": {},
                    "feeType": "FREE",
                    "accessibilityType": "UNISEX",
                    "lat": 55.7558,
                    "lon": 37.6176,
                    "dataSource": "MANUAL",
                    "amenities": {}
                }
                """.trimIndent()

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(minimalData)
                    }

                assertEquals(HttpStatusCode.Created, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("Minimal Address"))
            }
        }

    @Test
    @DisplayName("GIVEN invalid restroom data WHEN POST THEN return error response")
    fun given_invalid_restroom_data_when_post_then_return_error_response() =
        runTest {
            val invalidData = """{"invalid": "data"}"""

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.post("/api/v1/restrooms") {
                        contentType(ContentType.Application.Json)
                        setBody(invalidData)
                    }

                assertEquals(HttpStatusCode.BadRequest, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.contains("\"status\""))
            }
        }

    @Test
    @DisplayName("GIVEN existing restrooms WHEN GET nearest with valid coordinates THEN return nearest restrooms with distance")
    fun given_existing_restrooms_when_get_nearest_with_valid_coordinates_then_return_nearest_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            // Insert test restrooms with known coordinates
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 1",
                    lat = 55.7558,
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Restroom 2",
                    lat = 55.7559,
                    lon = 37.6177
                )
            )

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "5")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()
                assertTrue(body.startsWith("[") && body.endsWith("]"), "Response should be a JSON array")
                assertTrue(body.contains("Restroom 1") || body.contains("Restroom 2"), "Response should contain test restrooms")
                assertTrue(body.contains("distanceMeters"), "Response should contain distanceMeters field")
                assertTrue(body.contains("\"lat\""), "Response should contain lat field")
                assertTrue(body.contains("\"lon\""), "Response should contain lon field")
            }
        }

    @Test
    @DisplayName("GIVEN no restrooms WHEN GET nearest with valid coordinates THEN return empty array")
    fun given_no_restrooms_when_get_nearest_with_valid_coordinates_then_return_empty_array() =
        runTest {
            KtorTestApplication.withApp(testDatabase) { client ->
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
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            // Insert restrooms at different distances from search point (55.7558, 37.6176)
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Far Restroom",
                    lat = 55.7568, // ~111m away
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Close Restroom",
                    lat = 55.7559, // ~11m away
                    lon = 37.6176
                )
            )
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Medium Restroom",
                    lat = 55.7563, // ~55m away
                    lon = 37.6176
                )
            )

            KtorTestApplication.withApp(testDatabase) { client ->
                val response =
                    client.testGet(
                        "/api/v1/restrooms/nearest",
                        mapOf("lat" to "55.7558", "lon" to "37.6176", "limit" to "10")
                    )

                assertEquals(HttpStatusCode.OK, response.status)
                response.assertJsonContentType()
                val body = response.bodyAsText()

                // Parse JSON to validate structure and ordering
                val jsonArray =
                    kotlinx.serialization.json.Json
                        .decodeFromString<List<kotlinx.serialization.json.JsonObject>>(body)

                assertTrue(jsonArray.isNotEmpty(), "Should return at least one restroom")

                // Check that all items have required fields
                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    assertTrue(restroom.containsKey("lat"), "Each restroom should have lat field")
                    assertTrue(restroom.containsKey("lon"), "Each restroom should have lon field")
                    assertTrue(restroom.containsKey("name"), "Each restroom should have name field")

                    val distance = restroom["distanceMeters"]?.toString()?.toDoubleOrNull()
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                // Check that distances are sorted in ascending order (nearest first)
                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.toString()?.toDoubleOrNull() }
                val sortedDistances = distances.sorted()
                assertEquals(sortedDistances, distances, "Restrooms should be sorted by distance (nearest first)")

                // Verify that the closest restroom is returned first
                val firstRestroom = jsonArray.first()
                val firstName = firstRestroom["name"]?.toString()?.removeSurrounding("\"")
                assertTrue(firstName == "Close Restroom", "The closest restroom should be returned first")

                // Verify limit is respected and exact size
                assertTrue(jsonArray.size <= 10, "Response size should not exceed limit")
                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms")

                // Verify all restrooms are ACTIVE
                jsonArray.forEach { restroom ->
                    val status = restroom["status"]?.toString()?.removeSurrounding("\"")
                    assertEquals("ACTIVE", status, "All returned restrooms should have ACTIVE status")
                }

                // Verify approximate distance calculation for Close Restroom
                val closeRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Close Restroom"
                    }
                assertNotNull(closeRestroom, "Close Restroom should be found in results")
                val closeDistance = closeRestroom!!["distanceMeters"]?.toString()?.toDoubleOrNull()
                assertNotNull(closeDistance, "Close Restroom should have distanceMeters")
                // Expected distance: ~11m (55.7559 - 55.7558 = 0.0001 degrees ≈ 11m)
                assertTrue(closeDistance!! in 5.0..20.0, "Close Restroom distance should be approximately 11m (±15m tolerance)")

                // Verify approximate distance calculation for Medium Restroom
                val mediumRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Medium Restroom"
                    }
                assertNotNull(mediumRestroom, "Medium Restroom should be found in results")
                val mediumDistance = mediumRestroom!!["distanceMeters"]?.toString()?.toDoubleOrNull()
                assertNotNull(mediumDistance, "Medium Restroom should have distanceMeters")
                // Expected distance: ~55m (55.7563 - 55.7558 = 0.0005 degrees ≈ 55m)
                assertTrue(mediumDistance!! in 40.0..70.0, "Medium Restroom distance should be approximately 55m (±15m tolerance)")

                // Verify approximate distance calculation for Far Restroom
                val farRestroom =
                    jsonArray.find {
                        it["name"]?.toString()?.removeSurrounding("\"") == "Far Restroom"
                    }
                assertNotNull(farRestroom, "Far Restroom should be found in results")
                val farDistance = farRestroom!!["distanceMeters"]?.toString()?.toDoubleOrNull()
                assertNotNull(farDistance, "Far Restroom should have distanceMeters")
                // Expected distance: ~111m (55.7568 - 55.7558 = 0.001 degrees ≈ 111m)
                assertTrue(farDistance!! in 95.0..125.0, "Far Restroom distance should be approximately 111m (±15m tolerance)")

                // Verify distance ordering: Close < Medium < Far
                assertTrue(closeDistance < mediumDistance, "Close Restroom should be closer than Medium Restroom")
                assertTrue(mediumDistance < farDistance, "Medium Restroom should be closer than Far Restroom")
            }
        }

    @Test
    @DisplayName("GIVEN many restrooms WHEN GET nearest with small limit THEN return only limited results")
    fun given_many_restrooms_when_get_nearest_with_small_limit_then_return_only_limited_results() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)
            // Insert 10 restrooms but request only 3
            repeat(10) { i ->
                DatabaseTestHelper.insertTestRestroom(
                    testDatabase,
                    testEnv.cityId,
                    DatabaseTestHelper.createTestRestroomData(
                        name = "Restroom $i",
                        lat = 55.7558 + i * 0.001, // Spread them out
                        lon = 37.6176 + i * 0.001
                    )
                )
            }

            KtorTestApplication.withApp(testDatabase) { client ->
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

                // Verify exact limit is respected
                assertEquals(3, jsonArray.size, "Should return exactly 3 restrooms when limit is 3")

                // Verify all returned restrooms have required fields
                jsonArray.forEach { restroom ->
                    assertTrue(restroom.containsKey("distanceMeters"), "Each restroom should have distanceMeters field")
                    assertTrue(restroom.containsKey("status"), "Each restroom should have status field")

                    val status = restroom["status"]?.toString()?.removeSurrounding("\"")
                    assertEquals("ACTIVE", status, "All returned restrooms should have ACTIVE status")

                    val distance = restroom["distanceMeters"]?.toString()?.toDoubleOrNull()
                    assertTrue(distance != null && distance >= 0, "Distance should be a valid non-negative number")
                }

                // Verify distances are sorted (nearest first)
                val distances = jsonArray.mapNotNull { it["distanceMeters"]?.toString()?.toDoubleOrNull() }
                val sortedDistances = distances.sorted()
                assertEquals(sortedDistances, distances, "Restrooms should be sorted by distance (nearest first)")
            }
        }

    @Test
    @DisplayName("GIVEN active and inactive restrooms WHEN GET nearest THEN return only active restrooms")
    fun given_active_and_inactive_restrooms_when_get_nearest_then_return_only_active_restrooms() =
        runTest {
            val testEnv = DatabaseTestHelper.createTestEnvironment(testDatabase)

            // Insert an ACTIVE restroom
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Active Restroom",
                    lat = 55.7559, // Close to search point
                    lon = 37.6176
                )
            )

            // Insert an INACTIVE restroom (closer to search point)
            DatabaseTestHelper.insertTestRestroom(
                testDatabase,
                testEnv.cityId,
                DatabaseTestHelper.createTestRestroomData(
                    name = "Inactive Restroom",
                    lat = 55.7558, // Very close to search point
                    lon = 37.6176,
                    status = yayauheny.by.model.enums.RestroomStatus.INACTIVE
                )
            )

            KtorTestApplication.withApp(testDatabase) { client ->
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

                // Should return only 1 restroom (the active one)
                assertEquals(1, jsonArray.size, "Should return only 1 restroom (the active one)")

                // Verify the returned restroom is active
                val returnedRestroom = jsonArray.first()
                val status = returnedRestroom["status"]?.toString()?.removeSurrounding("\"")
                assertEquals("ACTIVE", status, "Returned restroom should be ACTIVE")

                val name = returnedRestroom["name"]?.toString()?.removeSurrounding("\"")
                assertEquals("Active Restroom", name, "Should return the active restroom, not the inactive one")

                // Verify distance is reasonable
                val distance = returnedRestroom["distanceMeters"]?.toString()?.toDoubleOrNull()
                assertNotNull(distance, "Active restroom should have distanceMeters")
                assertTrue(distance!! >= 0, "Distance should be non-negative")
            }
        }

    @Nested
    @DisplayName("Negative Test Cases")
    inner class NegativeTestCases {
        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with invalid UUID returns 400")
        fun get_restroom_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/restrooms/invalid-uuid")
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/city/{cityId} with invalid UUID returns 400")
        fun get_restrooms_by_city_invalid_uuid() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/restrooms/city/invalid-uuid")
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/{id} with non-existing UUID returns 404")
        fun get_restroom_non_existing_uuid() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val nonExistentId = "550e8400-e29b-41d4-a716-446655440000"
                    val response = client.testGet("/api/v1/restrooms/$nonExistentId")
                    assertEquals(HttpStatusCode.NotFound, response.status)
                    response.assertJsonContentType()
                    val body = response.bodyAsText()
                    assertTrue(body.contains("not found"))
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with invalid lat type returns 400")
        fun nearest_invalid_lat_type() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "abc", "lon" to "37.6"))
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("GET /api/v1/restrooms/nearest with out-of-range coordinates returns 400 with validation errors")
        fun nearest_out_of_range_coordinates() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testGet("/api/v1/restrooms/nearest", mapOf("lat" to "91.0", "lon" to "37.6"))
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()

                    val errorResponse = response.parseErrorResponse()
                    errorResponse.assertHasValidationErrors()
                    errorResponse.assertHasFieldError("lat", "не более 90 градусов")
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid JSON returns 400")
        fun post_restroom_invalid_json() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val response = client.testPost("/api/v1/restrooms", """{"invalid": "data"}""")
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }

        @Test
        @DisplayName("POST /api/v1/restrooms with invalid enum returns 400")
        fun post_restroom_invalid_enum() =
            runTest {
                KtorTestApplication.withApp(testDatabase) { client ->
                    val invalidEnumJson =
                        """
                        {
                            "name": "Test Restroom",
                            "address": "Test Address",
                            "feeType": "UNKNOWN",
                            "accessibilityType": "UNISEX",
                            "lat": 55.7,
                            "lon": 37.6,
                            "dataSource": "MANUAL",
                            "amenities": {}
                        }
                        """.trimIndent()
                    val response = client.testPost("/api/v1/restrooms", invalidEnumJson)
                    assertEquals(HttpStatusCode.BadRequest, response.status)
                    response.assertJsonContentType()
                }
            }
    }
}
